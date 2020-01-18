package com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime.BitflyerRealtimeAPIProtocol.SubscribeAuthParams
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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

}
