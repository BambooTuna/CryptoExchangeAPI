package com.github.BambooTuna.CryptoExchangeAPI

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer._
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.BitflyerRealtimeAPIProtocol._
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val apiKey = ???

  val realtimeAPI = BitflyerRealtimeAPI()

  val parsedMessageSink = Sink.foreach[JsonRpc] {
    case BitflyerRealtimeAPIProtocol.ConnectionOpened =>
      println("ConnectionOpened")
      realtimeAPI.authMessage(apiKey, 1)
      realtimeAPI.subscribeMessage(
        (BTCJPYExecutions, None)
      )
    case a: SignatureResult =>
      if (a.result && a.id.contains(1)) {
        realtimeAPI.subscribeMessage(
          (ChildOrderEvents, None),
          (ParentOrderEvents, None),
        )
      }
    case a: ReceivedChannelMessage[ExecutionsChannelParams] =>
      a.params.message.foreach(println)
    case a: ReceivedChannelMessage[OrderEventsChannelParams] =>
      a.params.message.foreach(println)
    case ParseError(origin) =>
      println(s"ParseError origin: $origin")
  }
  realtimeAPI.runBySink(parsedMessageSink)

}
