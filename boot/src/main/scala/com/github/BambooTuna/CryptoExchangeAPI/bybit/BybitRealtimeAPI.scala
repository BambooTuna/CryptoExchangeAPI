package com.github.BambooTuna.CryptoExchangeAPI.bybit

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime.BybitRealtimeAPIProtocol.{SubscribeAuthParams, SubscribeCommand}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.{ConnectionOpened, InternalFlowObject, SendMessage}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.{WebSocketStream, WebSocketStreamOptions}

import scala.concurrent.ExecutionContextExecutor
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.shapes._
import shapeless._

class BybitRealtimeAPI(apiAuth: Option[ApiAuth])(
  implicit system: ActorSystem,
  materializer: ActorMaterializer,
  executionContext: ExecutionContextExecutor) {

  private val options = WebSocketStreamOptions(
    host = "wss://stream.bybit.com/realtime",
    pingData = """{"op":"ping"}""",
    pongData =
      """{"op":"ping","args":null}"""
  )

  private val (ws: ActorRef, source: Source[InternalFlowObject, NotUsed]) =
    WebSocketStream.generateWebSocketFlowGraph(options)

  def runBySink(sink: Sink[String, Any]): Unit = {
    val completeSink = Sink.foreach[ConnectionOpened.type] { _ =>
      apiAuth.foreach(a => authMessage(a))
    }
    val runner = source via WebSocketStream.addCompleteSink(completeSink) to sink
    runner.run()
  }

  def sendMessage(message: String): Unit =
    ws ! SendMessage(message)

  def authMessage(apiAuth: ApiAuth): Unit =
    sendMessage(createAuthMessage(apiAuth))

  protected def createAuthMessage(apiAuth: ApiAuth): String =
    SubscribeCommand[List[String]](op = "auth", args = SubscribeAuthParams.create(apiAuth)).asJson.noSpaces

}
