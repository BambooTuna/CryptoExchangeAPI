package com.github.BambooTuna.CryptoExchangeAPI

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.{
  WebSocketStream,
  WebSocketStreamOptions
}

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val options = WebSocketStreamOptions(
    host = "wss://ws.lightstream.bitflyer.com/json-rpc",
    initMessage =
      """{"method":"subscribe","params":{"channel":"lightning_executions_FX_BTC_JPY"}}"""
  )
  val (actorRef, source) = WebSocketStream.generateWebSocketFlowGraph(options)
  val runner = source to Sink.foreach(println)
  runner run ()

}
