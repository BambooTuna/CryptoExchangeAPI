package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime.BitflyerRealtimeAPIProtocol.{
  BitflyerChannel,
  _
}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime.BitflyerRealtimeAPIProtocol
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.RealtimeAPIOptions
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPIResponseProtocol.{
  ConnectionOpened,
  ParseError,
  ParsedJsonResponse
}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamOptions

import scala.concurrent.ExecutionContextExecutor
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.shapes._
import shapeless._

object BitflyerRealtimeAPI {

  val defaultStreamOptions: WebSocketStreamOptions =
    WebSocketStreamOptions(
      host = "wss://ws.lightstream.bitflyer.com/json-rpc",
      pongData =
        """{"jsonrpc":"2.0","error":{"code":-32700,"message":"Parse error","data":"Invalid JSON"},"id":null}"""
    )

  def apply(apiAuth: Option[ApiAuth] = None)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor): BitflyerRealtimeAPI =
    new BitflyerRealtimeAPI(RealtimeAPIOptions(apiAuth, defaultStreamOptions))

}

class BitflyerRealtimeAPI(override val realtimeAPIOptions: RealtimeAPIOptions)(
    implicit override val system: ActorSystem,
    override val materializer: ActorMaterializer)
    extends RealtimeAPI[BitflyerChannel] {

  override protected def createSubscribeMessage(
      channel: BitflyerChannel): String =
    SubscribeCommand[BitflyerChannel](method = "subscribe",
                                      params = channel,
                                      id = channel.idOption).asJson.noSpaces

  override protected def createAuthMessage(apiAuth: ApiAuth): String =
    SubscribeCommand[SubscribeAuthParams](method = "auth",
                                          params =
                                            SubscribeAuthParams.create(apiAuth),
                                          id = Some(1)).asJson.noSpaces

  override protected def parseResponse(message: String): ParsedJsonResponse = {
    parser.decode[JsonrpcEvent](message) match {
      case Right(Inl(v))           => v
      case Right(Inr(Inl(v)))      => v
      case Right(Inr(Inr(Inl(v)))) => v
      case Left(e) =>
        message match {
          case "ConnectionOpened" =>
            ConnectionOpened
          case other => ParseError(e.getMessage, other)
        }
    }
  }

}
