package com.github.BambooTuna.CryptoExchangeAPI.bybit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime.BybitRealtimeAPIProtocol.{
  BybitChannel,
  JsonEvent,
  SubscribeAuthParams,
  SubscribeCommand
}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.{
  Channel,
  RealtimeAPIOptions
}
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
import shapeless._

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
    val a = SubscribeCommand[List[BybitChannel]](
      op = "subscribe",
      args = List(channel)).asJson.noSpaces
    println(a)
    a
  }

  override protected def createAuthMessage(apiAuth: ApiAuth): String =
    SubscribeCommand[List[String]](
      op = "auth",
      args = SubscribeAuthParams.create(apiAuth)).asJson.noSpaces

  override protected def parseResponse(message: String): ParsedJsonResponse = {
    parser.decode[JsonEvent](message) match {
      case Right(Inl(v))      => v
      case Right(Inr(Inl(v))) => v
      case Left(e) =>
        message match {
          case "ConnectionOpened" =>
            ConnectionOpened
          case other => ParseError(e.getMessage, other)
        }
    }
  }

}
