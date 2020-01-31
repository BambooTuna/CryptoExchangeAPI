package com.github.BambooTuna.CryptoExchangeAPI.bitmex

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.{
  RealtimeAPI,
  RealtimeAPIResponseProtocol
}
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.{
  Channel,
  RealtimeAPIOptions
}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamOptions

import scala.concurrent.ExecutionContextExecutor

object BitmexRealtimeAPI {

  val defaultStreamOptions: WebSocketStreamOptions =
    WebSocketStreamOptions(
      host = "wss://stream.bybit.com/realtime",
      pingData = """{"op":"ping"}""",
      pongData = """{"op":"ping","args":null}"""
    )

  def apply(apiAuth: Option[ApiAuth] = None)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor): BitmexRealtimeAPI =
    new BitmexRealtimeAPI(RealtimeAPIOptions(apiAuth, defaultStreamOptions))
}

class BitmexRealtimeAPI(override val realtimeAPIOptions: RealtimeAPIOptions)(
    implicit override val system: ActorSystem,
    override val materializer: ActorMaterializer)
    extends RealtimeAPI[Channel] {

  override protected def createSubscribeMessage(channel: Channel): String = ""

  override protected def createAuthMessage(apiAuth: ApiAuth): String = ""

  override protected def parseResponse(
      message: String): RealtimeAPIResponseProtocol.ParsedJsonResponse = ???

}
