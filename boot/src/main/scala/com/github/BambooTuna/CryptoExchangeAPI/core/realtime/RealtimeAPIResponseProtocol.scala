package com.github.BambooTuna.CryptoExchangeAPI.core.realtime

object RealtimeAPIResponseProtocol {
  trait ParsedJsonResponse
  case object ConnectionOpened extends ParsedJsonResponse
  case class ParseError(origin: String, message: String)
      extends ParsedJsonResponse
}
