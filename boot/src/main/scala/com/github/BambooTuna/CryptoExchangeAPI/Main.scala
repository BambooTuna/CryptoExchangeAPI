package com.github.BambooTuna.CryptoExchangeAPI

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import com.github.BambooTuna.CryptoExchangeAPI.bitmex.BitmexRealtimeAPI
import com.github.BambooTuna.CryptoExchangeAPI.bitmex.protocol.realtime.BitmexRealtimeAPIProtocol.{
  BitmexChannel,
  ConnectionInformation,
  SignatureResult
}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPIResponseProtocol.{
  ConnectionOpened,
  ParsedJsonResponse
}

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val bitmexApiKey =
    ApiAuth("key", "secret")
  val ws = BitmexRealtimeAPI(Some(bitmexApiKey))

  val parsedMessageSink = Sink.foreach[ParsedJsonResponse] {
    case ConnectionOpened =>
    case a: ConnectionInformation =>
      println(s"limit#remaining: ${a.limit.remaining}")
    case a: SignatureResult =>
      if (a.success && a.request.op == "authKeyExpires") {
        ws.subscribeChannel(
//          new BitmexChannel("orderBookL2_25", Some("XBTUSD")),
          new BitmexChannel("trade", Some("XBTUSD")),
        )
      }
    case other =>
      println(other)
  }

  ws.run(parsedMessageSink)

}
