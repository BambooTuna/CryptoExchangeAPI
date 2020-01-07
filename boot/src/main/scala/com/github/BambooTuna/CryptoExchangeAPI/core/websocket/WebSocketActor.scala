//package com.github.BambooTuna.CryptoExchangeAPI.core.websocket
//
//import akka.Done
//import akka.actor.SupervisorStrategy.{Restart, Stop}
//import akka.actor._
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.model.ws.{
//  BinaryMessage,
//  Message,
//  TextMessage,
//  WebSocketRequest
//}
//import akka.stream.{
//  ActorMaterializer,
//  ActorMaterializerSettings,
//  OverflowStrategy,
//  Supervision
//}
//import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
//import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketProtocol._
//
//import scala.concurrent.{ExecutionContextExecutor, Future}
//import scala.concurrent.duration._
//import scala.util.{Failure, Success}
//
//class WebSocketActor extends Actor {
//
//  implicit val system: ActorSystem = context.system
//  implicit val materializer: ActorMaterializer = ActorMaterializer()
//  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
//  var lastSender: ActorRef = context.system.deadLetters
//
//  override def receive = {
//    case ConnectStart(options) =>
//      lastSender = sender()
//      val ws = connect(options)
//      context.watch(ws)
//    case InternalException(e) => println(e)
//    case Closed => self ! InternalException("Closed")
//    case other => print(other)
//  }
//
//  def connect(options: WebSocketOptions): ActorRef = {
//    val ((ws, upgradeResponse), closed) =
//      wsRunner(options)(
//        Sink.foreach[Any] {
//          case m: ReceivedMessage   => lastSender ! m
//          case e: InternalException => self ! e
//        }
//      ).run()
//    closed.foreach(_ => self ! Closed)
//    val connected = upgradeResponse.flatMap { upgrade =>
//      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
//        Future.successful(Done)
//      } else {
//        Future.failed(
//          new Exception(s"Connection failed: ${upgrade.response.status}"))
//      }
//    }
//    connected.onComplete {
//      case Success(_) =>
//        options.logger.info("==========Connection Succeeded==========")
//        lastSender ! ConnectedSucceeded(ws)
////        ws ! TextMessage.Strict("""{"method":"subscribe","params":{"channel":"lightning_executions_FX_BTC_JPY"}}""")
//      case Failure(exception) =>
//        self ! InternalException(exception.getMessage)
//    }
//    ws
//  }
//
//  def wsRunner(options: WebSocketOptions)(sink: Sink[Any, Future[Done]]) = {
//    val req = WebSocketRequest(options.host)
//    val webSocketFlow = Http().webSocketClientFlow(req)
//    val messageSource: Source[Message, ActorRef] = Source
//      .actorRef[TextMessage.Strict](bufferSize = 1000, OverflowStrategy.fail)
////      .keepAlive(options.pingInterval, () => TextMessage.Strict(options.pingData))
//
//    val messageFlow = Flow[Message]
//      .collect {
//        case TextMessage.Strict(m) => Future.successful(ReceivedMessage(m))
//        case BinaryMessage.Strict(m) =>
//          Future.successful(ReceivedMessage(m.utf8String))
//        case TextMessage.Streamed(stream) =>
//          stream
//            .limit(100)
//            .completionTimeout(5.seconds)
//            .runFold("")(_ + _)
//            .flatMap(msg => Future.successful(ReceivedMessage(msg)))
//        case other =>
//          Future.successful(InternalException(s"Receive Strange Data: $other"))
//      }
//      .mapAsync(parallelism = 3)(identity)
//
//    messageSource
//      .viaMat(webSocketFlow)(Keep.both)
//      .via(messageFlow)
//      .toMat(sink)(Keep.both)
//
//  }
//
//  override def preStart() = {
//    super.preStart()
//    println("preStart")
//  }
//
//  override def postStop() = {
//    super.postStop()
//  }
//
//  override def supervisorStrategy = AllForOneStrategy() {
//    case _ => Restart
//  }
//
//}
//
//object WebSocketProtocol {
//
//  sealed trait I
//  case class ConnectStart(options: WebSocketOptions) extends I
//  case object Closed extends I
//  case class InternalException(value: String) extends I
//
//  sealed trait O
//  case class ConnectedSucceeded(actorRef: ActorRef) extends O
//  case class ReceivedMessage(value: String) extends O
//
////  case class InternalException(value: String) extends Exception(value)
//
//}
