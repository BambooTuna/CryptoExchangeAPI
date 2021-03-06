package com.github.BambooTuna.CryptoExchangeAPI.bitmex

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.BambooTuna.CryptoExchangeAPI.bitmex.protocol.realtime.BitmexRealtimeAPIProtocol.{
  BitmexChannel,
  BitmexJsonEvent,
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
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.InternalException
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.shapes._
import io.circe.parser
import shapeless._

import scala.concurrent.ExecutionContextExecutor

object BitmexRealtimeAPI {

  val defaultStreamOptions: WebSocketStreamOptions =
    WebSocketStreamOptions(
      host = "wss://www.bitmex.com/realtime",
      internalExceptionHandler = (e: InternalException) => {
        println(s"InternalException: ${e.value}")
      }
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
    extends RealtimeAPI[BitmexChannel] {

  override protected def createSubscribeMessage(
      channel: BitmexChannel): String =
    SubscribeCommand[List[BitmexChannel]](op = "subscribe",
                                          args = List(channel)).asJson.noSpaces

  override protected def createAuthMessage(apiAuth: ApiAuth): String =
    SubscribeCommand[List[String :+: Long :+: CNil]](
      op = "authKeyExpires",
      args = SubscribeAuthParams.create(apiAuth)).asJson.noSpaces

  override protected def parseResponse(message: String): ParsedJsonResponse = {
    parser.decode[BitmexJsonEvent](message) match {
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
