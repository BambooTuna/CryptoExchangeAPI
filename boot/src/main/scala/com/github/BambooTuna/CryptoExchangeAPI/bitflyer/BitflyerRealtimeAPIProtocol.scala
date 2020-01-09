package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import java.util.UUID

import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import io.circe.Encoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object BitflyerRealtimeAPIProtocol {
  case class SubscribeCommand[Params](method: String, params: Params)
  object SubscribeAuthParams {
    def create(apiAuth: ApiAuth): SubscribeAuthParams = {
      val timestamp = java.time.Instant.now().toEpochMilli
      val nonce = UUID.randomUUID.toString
      val signature = SubscribeAuthParams.HMACSHA256(s"$timestamp$nonce", apiAuth.secret)
      SubscribeAuthParams(apiAuth.key, timestamp, nonce, signature)
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
  case class SubscribeAuthParams(api_key: String, timestamp: Long, nonce: String = UUID.randomUUID.toString, signature: String)

  sealed class Channel(val channel: String)
  case object BTCJPYExecutions extends Channel("lightning_executions_BTC_JPY")
  case object FXBTCJPYExecutions extends Channel("lightning_executions_FX_BTC_JPY")
  case object BTCJPYBoard extends Channel("lightning_board_BTC_JPY")
  case object FXBTCJPYBoard extends Channel("lightning_board_FX_BTC_JPY")
  case object BTCJPYBoardSnapshot extends Channel("lightning_board_snapshot_BTC_JPY")
  case object FXBTCJPYBoardSnapshot extends Channel("lightning_board_snapshot_FX_BTC_JPY")

  case object ChildOrderEvents extends Channel("child_order_events")
  case object ParentOrderEvents extends Channel("parent_order_events")

  implicit val encodeUser: Encoder[Channel] =
    Encoder.forProduct1("channel")(_.channel)

}
