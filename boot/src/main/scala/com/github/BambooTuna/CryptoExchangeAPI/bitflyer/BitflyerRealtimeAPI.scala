package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime.BitflyerRealtimeAPIProtocol._
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime.BitflyerRealtimeAPIProtocol
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.{
  ConnectionOpened,
  InternalFlowObject,
  SendMessage
}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.{
  WebSocketStream,
  WebSocketStreamOptions
}

import scala.concurrent.ExecutionContextExecutor
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.shapes._
import shapeless._

object BitflyerRealtimeAPI {
  def apply(apiAuth: ApiAuth)(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executionContext: ExecutionContextExecutor): BitflyerRealtimeAPI =
    new BitflyerRealtimeAPI(Some(apiAuth))

  def apply()(implicit system: ActorSystem,
              materializer: ActorMaterializer,
              executionContext: ExecutionContextExecutor): BitflyerRealtimeAPI =
    new BitflyerRealtimeAPI(None)

}

class BitflyerRealtimeAPI(apiAuth: Option[ApiAuth])(
    implicit system: ActorSystem,
    materializer: ActorMaterializer,
    executionContext: ExecutionContextExecutor) {

  private val options = WebSocketStreamOptions(
    host = "wss://ws.lightstream.bitflyer.com/json-rpc",
    pongData =
      """{"jsonrpc":"2.0","error":{"code":-32700,"message":"Parse error","data":"Invalid JSON"},"id":null}"""
  )
  private val (ws: ActorRef, source: Source[InternalFlowObject, NotUsed]) =
    WebSocketStream.generateWebSocketFlowGraph(options)

  def runBySink(sink: Sink[JsonRpc, Any]): Unit = {
    val completeSink = Sink.foreach[ConnectionOpened.type] { _ =>
      apiAuth.foreach(a => authMessage(a, id = 1))
    }
    val runner = source via WebSocketStream.addCompleteSink(completeSink) via Flow[
      String].map(messageParser) to sink
    runner.run()
  }

  def sendMessage(message: String): Unit =
    ws ! SendMessage(message)

  def sendMessage(messages: Seq[String]): Unit =
    messages.foreach(sendMessage)

  def subscribeMessage(channels: (Channel, Option[Int])*): Unit =
    sendMessage(createSubscribeMessage(channels = channels))

  def authMessage(apiAuth: ApiAuth, id: Int): Unit =
    sendMessage(createAuthMessage(apiAuth, id))

  protected def createSubscribeMessage(channel: Channel,
                                       id: Option[Int] = None): String =
    SubscribeCommand[Channel](method = "subscribe", params = channel, id = id).asJson.noSpaces

  protected def createSubscribeMessage(
      channels: Seq[(Channel, Option[Int])]): Seq[String] =
    channels.map(a => createSubscribeMessage(a._1, a._2))

  protected def createAuthMessage(apiAuth: ApiAuth, id: Int): String =
    SubscribeCommand[SubscribeAuthParams](method = "auth",
                                          params =
                                            SubscribeAuthParams.create(apiAuth),
                                          id = Some(id)).asJson.noSpaces

  protected def messageParser(message: String): JsonRpc = {
    parser.decode[JsonrpcEvent](message) match {
      case Right(Inl(v))           => v
      case Right(Inr(Inl(v)))      => v
      case Right(Inr(Inr(Inl(v)))) => v
      case _ =>
        message match {
          case "ConnectionOpened" =>
            BitflyerRealtimeAPIProtocol.ConnectionOpened
          case other => ParseError(other)
        }
    }
  }

}
