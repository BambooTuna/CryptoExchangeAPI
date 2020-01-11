package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Broadcast, Flow, RunnableGraph, Sink, Source}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.BitflyerRealtimeAPIProtocol._
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.{
  ConnectionOpened,
  InternalFlowObject,
  ParsedMessage,
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

  def runBySink(sink: Sink[String, Any]): Unit = {
    val completeSink = Sink.foreach[ConnectionOpened.type] { _ =>
      println("runBySink ConnectionOpened")
      apiAuth
        .map(a => authMessage(a, id = 1))
        .foreach(sendMessage)
    }
    val runner = source via WebSocketStream.addCompleteSink(completeSink) to sink
    runner.run()
  }

  def sendMessage(message: String): Unit = {
    println(SendMessage(message))
    ws ! SendMessage(message)
  }

  def messageParser(message: String): ReceivedChannelMessage[ExecutionsChannelParams] = {
    parser.decode[ReceivedChannelMessage[ExecutionsChannelParams]](message).right.get
//    for {
//      json <- parser.parse(message)
//      action <- json.hcursor.downField("params").downField("message").as[List[LightningExecutions]]
//    } yield ()
  }

  def subscribeMessage(channel: Channel, id: Option[Int] = None): String =
    SubscribeCommand[Channel](method = "subscribe", params = channel, id = id).asJson.noSpaces

  def authMessage(apiAuth: ApiAuth, id: Int): String =
    SubscribeCommand[SubscribeAuthParams](method = "auth",
                                          params =
                                            SubscribeAuthParams.create(apiAuth),
                                          id = Some(id)).asJson.noSpaces

}
