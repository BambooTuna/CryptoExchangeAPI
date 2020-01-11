package com.github.BambooTuna.CryptoExchangeAPI

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.BitflyerRealtimeAPI
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.BitflyerRealtimeAPIProtocol._
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val realtimeAPI = BitflyerRealtimeAPI(ApiAuth("", ""))

  val parsedMessageSink = Sink.foreach[String] { s =>
    println(s"parsedMessageSink: $s")
    if (s == """{"jsonrpc":"2.0","id":1,"result":true}""") {
      realtimeAPI.sendMessage(
        realtimeAPI.subscribeMessage(ChildOrderEvents)
      )
      realtimeAPI.sendMessage(
        realtimeAPI.subscribeMessage(ParentOrderEvents)
      )
    }

    if (s == "ConnectionOpened") {
      realtimeAPI.sendMessage(
        realtimeAPI.subscribeMessage(BTCJPYExecutions)
      )
    }
  }
  realtimeAPI.runBySink(parsedMessageSink)

}
