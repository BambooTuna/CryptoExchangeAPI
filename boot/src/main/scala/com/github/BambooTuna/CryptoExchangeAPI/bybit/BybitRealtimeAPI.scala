package com.github.BambooTuna.CryptoExchangeAPI.bybit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime.BybitRealtimeAPIProtocol.{
  BybitChannel,
  BybitJsonEvent,
  SubscribeAuthParams,
  SubscribeCommand
}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.RealtimeAPIOptions
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPIResponseProtocol.{
  ConnectionOpened,
  ParseError,
  ParsedJsonResponse
}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamOptions

import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.shapes._
import io.circe.parser
import shapeless.Coproduct

import scala.concurrent.ExecutionContextExecutor

object BybitRealtimeAPI {

  val defaultStreamOptions: WebSocketStreamOptions =
    WebSocketStreamOptions(
      host = "wss://stream.bybit.com/realtime",
      pingData = """{"op":"ping"}""",
      pongData = """{"op":"ping","args":null}"""
    )

  def apply(apiAuth: Option[ApiAuth] = None)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor): BybitRealtimeAPI =
    new BybitRealtimeAPI(RealtimeAPIOptions(apiAuth, defaultStreamOptions))

}

class BybitRealtimeAPI(override val realtimeAPIOptions: RealtimeAPIOptions)(
    implicit override val system: ActorSystem,
    override val materializer: ActorMaterializer)
    extends RealtimeAPI[BybitChannel] {

  override protected def createSubscribeMessage(
      channel: BybitChannel): String = {
    SubscribeCommand[List[BybitChannel]](op = "subscribe", args = List(channel)).asJson.noSpaces
  }

  override protected def createAuthMessage(apiAuth: ApiAuth): String =
    SubscribeCommand[List[String]](
      op = "auth",
      args = SubscribeAuthParams.create(apiAuth)).asJson.noSpaces

  override protected def parseResponse(message: String): ParsedJsonResponse = {
    parser.decode[BybitJsonEvent](message) match {
      case Right(v) => Coproduct.unsafeGet(v).asInstanceOf[ParsedJsonResponse]
      case Left(e) =>
        message match {
          case "ConnectionOpened" =>
            ConnectionOpened
          case other => ParseError(e.getMessage, other)
        }
    }
  }

}
