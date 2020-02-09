package com.github.BambooTuna.CryptoExchangeAPI.core.websocket

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{
  BinaryMessage,
  Message,
  TextMessage,
  WebSocketRequest
}
import akka.stream.scaladsl.{
  Broadcast,
  BroadcastHub,
  Flow,
  GraphDSL,
  Keep,
  Merge,
  RestartSource,
  Sink,
  Source,
  Zip
}
import akka.stream.{
  ActorMaterializer,
  FlowShape,
  Graph,
  OverflowStrategy,
  SourceShape
}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol._

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object WebSocketStream {

  def generateWebSocketFlowGraph(options: WebSocketStreamOptions)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor)
    : (ActorRef, Source[InternalFlowObject, NotUsed]) = {
    val (actorRef, source) =
      Source
        .actorRef[InternalFlowObject](bufferSize = 1000, OverflowStrategy.fail)
        .toMat(BroadcastHub.sink[InternalFlowObject](bufferSize = 256))(
          Keep.both)
        .run()

    val allSource =
      if (options.reConnect) {
        RestartSource.withBackoff(minBackoff = 1.second,
                                  maxBackoff = options.reConnectInterval,
                                  0.2,
                                  -1) { () =>
          Source.fromGraph(webSocketFlowGraph(options, source))
        }
      } else Source.fromGraph(webSocketFlowGraph(options, source))

    (actorRef, allSource)
  }

  def addCompleteSink(completeSink: Sink[ConnectionOpened.type, Any])
    : Graph[FlowShape[InternalFlowObject, String], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[InternalFlowObject](2))

      val parsedMessageFilterFlow = builder.add(
        Flow[InternalFlowObject].collect {
          case ParsedMessage(value) => value
          case ConnectionOpened     => "ConnectionOpened"
        }
      )
      val completeFilterFlow = Flow[InternalFlowObject]
        .collectType[ConnectionOpened.type]

      broadcast ~> parsedMessageFilterFlow
      broadcast ~> completeFilterFlow ~> completeSink

      FlowShape(broadcast.in, parsedMessageFilterFlow.out)
    }
  }

  private def webSocketFlowGraph(options: WebSocketStreamOptions,
                                 source: Source[InternalFlowObject, NotUsed])(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor)
    : Graph[SourceShape[InternalFlowObject], NotUsed] =
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val marge = builder.add(Merge[InternalFlowObject](3))
      val broadcast = builder.add(Broadcast[InternalFlowObject](3))

      val webSocketHandlerFlow = Flow[InternalFlowObject].collect {
        case e: InternalException =>
          options.internalExceptionHandler(e)
          throw new Exception(e.value)
        case m: SendMessage       => m
      }

      val webSocketFilterMapFlow = Flow[InternalFlowObject].collect {
        case SendMessage(m) => TextMessage.Strict(m)
      }
      val webSocketFlow = webSocketHandlerFlow
        .via(webSocketFilterMapFlow)
        .viaMat(createWebSocketFlow(options))(Keep.right)
        .via(Flow[Message].map(m => ReceivedMessage(m)))
      val handledWebSocketFlow =
        mergeMaterializedValueFlow[InternalFlowObject](webSocketFlow)

      val parseMessageFlow = builder.add(
        Flow[InternalFlowObject]
          .collect {
            case ReceivedMessage(TextMessage.Strict(m)) =>
              Future.successful(ParsedMessage(m))
            case ReceivedMessage(BinaryMessage.Strict(m)) =>
              Future.successful(ParsedMessage(m.utf8String))
            case ReceivedMessage(TextMessage.Streamed(stream)) =>
              stream
                .limit(100)
                .completionTimeout(10.seconds)
                .runFold("")(_ + _)
                .flatMap(msg => Future.successful(ParsedMessage(msg)))
            case ConnectionOpened => Future.successful(ConnectionOpened)
          }
          .mapAsync(parallelism = 16)(identity)
          .filterNot {
            case ParsedMessage(v) => v.contains(options.pongData)
            case _                => false
          }
      )

      val callBackFilterMapFlow = builder.add(
        Flow[InternalFlowObject].collect {
          case a: InternalException => a
        }
      )

      val regularMonitoringFilterFlow =
        Flow[InternalFlowObject].collectType[ReceivedMessage]
      val regularMonitoringFlow =
        Flow
          .fromGraph(
            createRegularMonitoringFlow[ReceivedMessage](
              pingInterval = options.pingInterval,
              pingTimeout = options.pingTimeout))
          .keepAlive(options.pingInterval, () => SendMessage(options.pingData))

      source ~> marge ~> handledWebSocketFlow ~> broadcast ~> parseMessageFlow
      marge <~ callBackFilterMapFlow <~ broadcast
      marge <~ regularMonitoringFlow <~ regularMonitoringFilterFlow <~ broadcast

      SourceShape(parseMessageFlow.out)
    }

  private def createRegularMonitoringFlow[In](pingInterval: FiniteDuration,
                                              pingTimeout: FiniteDuration)
    : Graph[FlowShape[In, InternalException], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val bufferSize = (pingTimeout / pingInterval).toInt + 1

      val mainFlow = builder.add(
        Flow[In].buffer(size = bufferSize, OverflowStrategy.dropHead))

      val subSource = Source.repeat(1).throttle(1, pingInterval)
      val subFlow = Flow[Int].buffer(size = bufferSize, OverflowStrategy.fail)

      val mergeZip = builder.add(Zip[In, Int])
      val takeFlow = builder.add(
        Flow[(In, Int)]
          .throttle(1, pingInterval)
          .map(_._1)
          .collect[InternalException] {
            case e: InternalException => e
          }
      )

      mainFlow ~> mergeZip.in0
      subSource ~> subFlow ~> mergeZip.in1
      mergeZip.out ~> takeFlow

      FlowShape(mainFlow.in, takeFlow.out)
    }
  }

  private def mergeMaterializedValueFlow[In](
      mainFlow: Flow[In, InternalFlowObject, ConnectionOpenedFuture])(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor)
    : Graph[FlowShape[In, InternalFlowObject], InternalFlowObject] = {
    GraphDSL.create(mainFlow) { implicit builder => flow =>
      import GraphDSL.Implicits._
      val merge = builder.add(Merge[InternalFlowObject](2))
      val parseFlow = builder.materializedValue via Flow[ConnectionOpenedFuture]
        .mapAsync[InternalFlowObject](parallelism = 2)(_.value)
      parseFlow ~> merge
      flow.out ~> merge
      FlowShape(flow.in, merge.out)
    }
  }

  private def createWebSocketFlow(options: WebSocketStreamOptions)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor)
    : Flow[Message, Message, ConnectionOpenedFuture] = {
    val req = WebSocketRequest(options.host)
    Http()
      .webSocketClientFlow(request = req)
      .mapMaterializedValue(f => {
        val connected = f
          .map { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols)
              ConnectionOpened
            else
              InternalException(
                s"ConnectionFailed status: ${upgrade.response.status}")
          }
          .recover { case _ => InternalException("ConnectionFailed") }
        ConnectionOpenedFuture(
          Future { Await.result(connected, 5.seconds) }
            .recover { case _ => InternalException("ConnectionTimeOut") }
        )
      })
  }

}
