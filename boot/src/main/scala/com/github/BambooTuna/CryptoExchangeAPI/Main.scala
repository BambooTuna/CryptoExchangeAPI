package com.github.BambooTuna.CryptoExchangeAPI

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, WebSocketRequest}
import akka.stream.{ActorAttributes, ActorMaterializer, ActorMaterializerSettings, FlowShape, Graph, Materializer, OverflowStrategy, SourceShape, Supervision}
import akka.stream.scaladsl.{GraphDSL, _}
import com.github.BambooTuna.CryptoExchangeAPI.WebSocketActor._
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketOptions
import monix.eval.Task

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

object Main extends App {

  val decider: Supervision.Decider = {
//    case _: ArithmeticException => Supervision.Restart
    case _ => Supervision.Stop
  }
  val decider2: Supervision.Decider = {
    case _ => Supervision.Restart
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)) //ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val messageSource: Source[InternalFlowObject, ActorRef] =
    Source
      .actorRef[InternalFlowObject](bufferSize = 1000, OverflowStrategy.fail)
  val (actorRef, source) =
    messageSource
      .toMat(BroadcastHub.sink[InternalFlowObject](bufferSize = 256))(Keep.both)
      .run()

  val options = WebSocketOptions(
    host = "wss://ws.lightstream.bitflyer.com/json-rpc")

  Source.fromGraph(webSocketFlowGraph(options)(source)) runForeach(println)

//
//  RestartSource.withBackoff(minBackoff = 5.second, maxBackoff = 10.second, 0.2, -1) { () =>
//    Source(1 to 10) via
//      Flow[Int].map { i =>
//        if (i < 5) i else throw new Exception("error")
//      }
//  } to Sink.foreach(println) run()



//  Thread.sleep(10000)
//  println("throw error")
//  actorRef ! InternalException("force")

//  Thread.sleep(1000 * 120)
//  println("throw error 120")
//  actorRef ! InternalException("force2")

  def mergeMaterializedValueFlow[In](
      mainFlow: Flow[In, InternalFlowObject, ConnectionOpenedFuture])
    : Graph[FlowShape[In, InternalFlowObject], InternalFlowObject] = {
    GraphDSL.create(mainFlow) { implicit builder => flow =>
      import GraphDSL.Implicits._
      val merge = builder.add(Merge[InternalFlowObject](2))
      val parseFlow = builder.materializedValue via Flow[ConnectionOpenedFuture]
        .mapAsync[InternalFlowObject](parallelism = 2)(f => {
          val future =
            f.value
          future.onComplete(a => println(s"parseFlow: $a"))
          future
        })
      parseFlow ~> merge
      flow.out ~> merge
      FlowShape(flow.in, merge.out)
    }
  }

  def createWebSocketFlow(options: WebSocketOptions)
    : Flow[Message, Message, ConnectionOpenedFuture] = {
    val req = WebSocketRequest(options.host)
    Http()
      .webSocketClientFlow(request = req)
      .mapMaterializedValue(f => {
        val connected = f
          .map { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols)
              ConnectionOpened
            else {
              println(s"ConnectionFailed status: ${upgrade.response.status}")
              InternalException(
                s"ConnectionFailed status: ${upgrade.response.status}")
            }
          }
          .recover { case _ =>
            println("ConnectionFailed")
            InternalException("ConnectionFailed")
          }
        ConnectionOpenedFuture(connected)
      })
  }

  def webSocketFlowGraph(options: WebSocketOptions)(source: Source[InternalFlowObject, NotUsed]): Graph[SourceShape[InternalFlowObject], NotUsed] =
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val marge = builder.add(Merge[InternalFlowObject](2))
      val broadcast = builder.add(Broadcast[InternalFlowObject](2))

      val webSocketHandlerFlow = Flow[InternalFlowObject].collect {
        case InternalException(e) =>
          println(s"InternalException: $e")
          throw new Exception(e)
        case m: SendMessage =>
          println(s"SendMessage: $m")
          m
      }
      val webSocketFilterFlow = Flow[InternalFlowObject].collect {
        case SendMessage(m) => TextMessage.Strict(m)
      }

      val test = webSocketHandlerFlow
        .via(webSocketFilterFlow)
        .viaMat(createWebSocketFlow(options))(Keep.right)
        .via(Flow[Message].map(m => ReceivedMessage(m)))

      val webSocketFlow =
        mergeMaterializedValueFlow[InternalFlowObject](test)

      val handledWebSocketFlow =
        RestartFlow.withBackoff(minBackoff = 1.second, maxBackoff = 1.second, 0.2, -1) { () =>
          Flow[InternalFlowObject] via webSocketFlow
        }

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
            case ReceivedMessage(other) =>
              Future.successful(
                InternalException(s"Receive Strange Data: $other"))
          }
          .mapAsync(parallelism = 3)(identity)
      )

      val internalActionFlow = builder.add(
        Flow[InternalFlowObject].collect {
          case ConnectionOpened => SendMessage(options.initMessage)
        }
      )

      source ~> marge ~> handledWebSocketFlow ~> broadcast ~> parseMessageFlow
      marge <~ internalActionFlow <~ broadcast
      SourceShape(parseMessageFlow.out)
    }

}

object WebSocketActor {
  sealed trait InternalFlowObject
  case class ReceivedMessage(message: Message) extends InternalFlowObject
  case class ParsedMessage(value: String) extends InternalFlowObject
  case class SendMessage(value: String) extends InternalFlowObject
  case class ConnectionOpenedFuture(value: Future[InternalFlowObject])
      extends InternalFlowObject
  case object ConnectionOpened extends InternalFlowObject
  case class InternalException(value: String) extends InternalFlowObject
}
