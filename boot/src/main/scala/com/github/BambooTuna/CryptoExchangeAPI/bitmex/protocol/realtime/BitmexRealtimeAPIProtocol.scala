package com.github.BambooTuna.CryptoExchangeAPI.bitmex.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime.BybitRealtimeAPIProtocol.SignatureResult
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.Channel
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPIResponseProtocol.ParsedJsonResponse
import io.circe._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import shapeless._

object BitmexRealtimeAPIProtocol {

  case class SubscribeCommand[Params](op: String, args: Params)
  case class SubscribeCommandOp(op: String)

  object SubscribeAuthParams {
    def create(apiAuth: ApiAuth): List[String :+: Long :+: CNil] = {
      val timestamp = java.time.Instant.now().toEpochMilli + 5000
      val signature = HMACSHA256(s"GET/realtime$timestamp", apiAuth.secret)
      List(
        Coproduct[String :+: Long :+: CNil](apiAuth.key),
        Coproduct[String :+: Long :+: CNil](timestamp),
        Coproduct[String :+: Long :+: CNil](signature)
      )
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

  sealed class BitmexChannel(val channel: String,
                             val symbol: Option[String] = None)
      extends Channel

  implicit val encodeUser: Encoder[BitmexChannel] =
    Encoder.instance[BitmexChannel](a =>
      Json.fromString(s"${a.channel}${a.symbol.map(":" + _).getOrElse("")}"))

  case class SignatureResult(success: Boolean, request: SubscribeCommandOp)
      extends ParsedJsonResponse

  case class ConnectionLimit(remaining: Long)
  case class ConnectionInformation(limit: ConnectionLimit)
      extends ParsedJsonResponse

  case class ReceivedChannelMessage[Params](table: String,
                                            action: String,
                                            data: Params)
      extends ParsedJsonResponse

  type BitmexJsonEvent =
    SignatureResult :+: ConnectionInformation :+: ReceivedChannelMessage[
      List[TradeData]] :+: CNil

}
