package com.github.BambooTuna.CryptoExchangeAPI.core.websocket

import com.github.BambooTuna.CryptoExchangeAPI.core.websocket.WebSocketStreamProtocol.InternalException
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

case class WebSocketStreamOptions(
    host: String,
    reConnect: Boolean = true,
    reConnectInterval: FiniteDuration = 5.seconds,
    pingInterval: FiniteDuration = 5.seconds,
    pingTimeout: FiniteDuration = 30.seconds,
    pingData: String = "ping",
    pongData: String = "pong",
    internalExceptionHandler: InternalException => Unit = println
) {
  require(pingTimeout > pingInterval)
  require(host.nonEmpty)
}
