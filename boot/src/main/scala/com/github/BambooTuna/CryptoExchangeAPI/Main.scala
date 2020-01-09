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

  val sink = Sink.foreach[String](println)
  realtimeAPI.runBySink(sink)
  realtimeAPI.sendMessage(
    realtimeAPI.subscribeMessage(FXBTCJPYExecutions)
  )

}
