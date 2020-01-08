package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.BitflyerRealtimeAPI._
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.{ParsedMessage, SendMessage}
import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.{WebSocketStream, WebSocketStreamOptions}

import scala.concurrent.ExecutionContextExecutor

class BitflyerRealtimeAPI(
                           implicit system: ActorSystem,
                           materializer: ActorMaterializer,
                           executionContext: ExecutionContextExecutor) {

  private val options = WebSocketStreamOptions(
    host = "wss://ws.lightstream.bitflyer.com/json-rpc",
    pongData = """{"jsonrpc":"2.0","error":{"code":-32700,"message":"Parse error","data":"Invalid JSON"},"id":null}""",
    initMessage = subscribeMessage(BTCJPYExecutions)
  )
  private val (ws: ActorRef, source: Source[ParsedMessage, NotUsed]) = WebSocketStream.generateWebSocketFlowGraph(options)

  def runBySink(sink: Sink[String, Any]): Unit = {
    val runner = source.map(_.value) to sink
    runner run ()
  }

  def sendMessage(message: String): Unit =
    ws ! SendMessage(message)

  def subscribeMessage(channel: PublicChannel): String =
    s"""{"method":"subscribe","params":{"channel":"${channel.value}"}}"""

}

object BitflyerRealtimeAPI {
  sealed class PublicChannel(val value: String)
  case object BTCJPYExecutions extends PublicChannel("lightning_executions_BTC_JPY")
  case object FXBTCJPYExecutions extends PublicChannel("lightning_executions_FX_BTC_JPY")
  case object BTCJPYBoard extends PublicChannel("lightning_board_BTC_JPY")
  case object FXBTCJPYBoard extends PublicChannel("lightning_board_FX_BTC_JPY")
  case object BTCJPYBoardSnapshot extends PublicChannel("lightning_board_snapshot_BTC_JPY")
  case object FXBTCJPYBoardSnapshot extends PublicChannel("lightning_board_snapshot_FX_BTC_JPY")
}
