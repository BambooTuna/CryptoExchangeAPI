package com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.Channel
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPIResponseProtocol.ParsedJsonResponse
import io.circe.{Encoder, Json}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import shapeless._

object BybitRealtimeAPIProtocol {

  case class SubscribeCommand[Params](op: String, args: Params)

  object SubscribeAuthParams {
    def create(apiAuth: ApiAuth): List[String] = {
      val timestamp = java.time.Instant.now().toEpochMilli + 1000
      val signature = HMACSHA256(s"GET/realtime$timestamp", apiAuth.secret)
      List(apiAuth.key, timestamp.toString, signature)
    }

    def HMACSHA256(text: String, secret: String): String = {
      val algorithm = "HMacSha256"
      val secretKey = new SecretKeySpec(secret.getBytes, algorithm)
      val mac = Mac.getInstance(algorithm)
      mac.init(secretKey)
      mac
        .doFinal(text.getBytes)
        .foldLeft("")((l, r) =>
          l + Integer.toString((r & 0xff) + 0x100, 16).substring(1))
    }
  }

  sealed class BybitChannel(val channel: String,
                            val symbol: Option[String] = None)
      extends Channel

  implicit val encodeUser: Encoder[BybitChannel] =
    Encoder.instance[BybitChannel](a =>
      Json.fromString(s"${a.channel}${a.symbol.map("." + _).getOrElse("")}"))

  case class SignatureResult(success: Boolean) extends ParsedJsonResponse
  case class ReceivedChannelMessage[Params](topic: String, data: Params)
      extends ParsedJsonResponse
  type BybitJsonEvent =
    SignatureResult :+: ReceivedChannelMessage[List[TradeData]] :+: ReceivedChannelMessage[
      List[OrderBookData]] :+: ReceivedChannelMessage[OrderBooks] :+: CNil

}
