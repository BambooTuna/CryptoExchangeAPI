package com.github.BambooTuna.CryptoExchangeAPI.core.websocket

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

case class WebSocketStreamOptions(
    host: String = "",
    initMessage: String = "",
    reConnect: Boolean = true,
    reConnectInterval: FiniteDuration = 5.seconds,
    pingInterval: FiniteDuration = 5.seconds,
    pingTimeout: FiniteDuration = 10.seconds,
    pingData: String = "ping",
    pongData: String = "pong",
    logger: Logger = LoggerFactory.getLogger("WebSocketManager")
) {
  require(pingTimeout > pingInterval)
}
