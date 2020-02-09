package com.github.BambooTuna.CryptoExchangeAPI.core.realtime

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.{
  Channel,
  RealtimeAPIOptions
}
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPIResponseProtocol.{
  ParseError,
  ParsedJsonResponse
}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.{
  WebSocketStream,
  WebSocketStreamOptions,
  WebSocketStreamProtocol
}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.{
  InternalFlowObject,
  SendMessage
}
import io.circe.generic.auto._
import io.circe.parser
import shapeless._

import scala.concurrent.ExecutionContextExecutor

trait RealtimeAPI[C <: Channel] {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val realtimeAPIOptions: RealtimeAPIOptions

  private val completeSink
    : Sink[WebSocketStreamProtocol.ConnectionOpened.type, Any] =
    Sink.foreach[WebSocketStreamProtocol.ConnectionOpened.type] { _ =>
      realtimeAPIOptions.apiAuth.foreach(authentication)
    }
  private val (ws: ActorRef, source: Source[InternalFlowObject, NotUsed]) =
    WebSocketStream.generateWebSocketFlowGraph(realtimeAPIOptions.options)

  def run(sink: Sink[ParsedJsonResponse, Any]): Unit = {
    val runner: RunnableGraph[NotUsed] = source via WebSocketStream
      .addCompleteSink(completeSink) via Flow[String].map(parseResponse) to sink
    runner.run()
  }

  def sendMessage(message: String): Unit =
    ws ! SendMessage(message)

  def sendMessage(messages: String*): Unit =
    messages.foreach(sendMessage)

  def subscribeChannel(channel: C): Unit =
    sendMessage(createSubscribeMessage(channel))

  def subscribeChannel(channel: C*): Unit =
    channel.foreach(subscribeChannel)

  def authentication(apiAuth: ApiAuth): Unit =
    sendMessage(createAuthMessage(apiAuth))

  protected def createSubscribeMessage(channel: C): String

  protected def createAuthMessage(apiAuth: ApiAuth): String

  protected def parseResponse(message: String): ParsedJsonResponse

}

object RealtimeAPI {
  case class RealtimeAPIOptions(apiAuth: Option[ApiAuth],
                                options: WebSocketStreamOptions)
  trait Channel
}
