package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.BitflyerRealtimeAPIProtocol._
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.{ParsedMessage, SendMessage}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.{WebSocketStream, WebSocketStreamOptions}

import scala.concurrent.ExecutionContextExecutor

import io.circe.syntax._
import io.circe.generic.auto._

object BitflyerRealtimeAPI {
  def apply(apiAuth: ApiAuth)(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor): BitflyerRealtimeAPI =
    new BitflyerRealtimeAPI(Some(apiAuth))(system, materializer, executionContext)

  def apply(implicit system: ActorSystem, materializer: ActorMaterializer, executionContext: ExecutionContextExecutor): BitflyerRealtimeAPI =
    new BitflyerRealtimeAPI(None)(system, materializer, executionContext)
}

class BitflyerRealtimeAPI(apiAuth: Option[ApiAuth])(
                           implicit system: ActorSystem,
                           materializer: ActorMaterializer,
                           executionContext: ExecutionContextExecutor) {

  private val options = WebSocketStreamOptions(
    host = "wss://ws.lightstream.bitflyer.com/json-rpc",
    pongData = """{"jsonrpc":"2.0","error":{"code":-32700,"message":"Parse error","data":"Invalid JSON"},"id":null}""",
    initMessage = apiAuth.map(authMessage)
  )
  private val (ws: ActorRef, source: Source[ParsedMessage, NotUsed]) = WebSocketStream.generateWebSocketFlowGraph(options)

  def runBySink(sink: Sink[String, Any]): Unit = {
    val runner = source.map(_.value) to sink
    runner run ()
  }

  def sendMessage(message: String): Unit =
    ws ! SendMessage(message)

  def subscribeMessage(channel: Channel): String =
    SubscribeCommand[Channel]("subscribe", channel).asJson.noSpaces

  def authMessage(apiAuth: ApiAuth): String =
    SubscribeCommand[SubscribeAuthParams]("auth", SubscribeAuthParams.create(apiAuth)).asJson.noSpaces

}
