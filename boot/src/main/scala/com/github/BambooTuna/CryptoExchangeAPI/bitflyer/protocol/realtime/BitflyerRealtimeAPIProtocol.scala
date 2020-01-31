package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPI.Channel
import com.github.BambooTuna.CryptoExchangeAPI.core.realtime.RealtimeAPIResponseProtocol.ParsedJsonResponse
import io.circe.Encoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import shapeless._

object BitflyerRealtimeAPIProtocol {

  case class SubscribeCommand[Params](jsonrpc: String = "2.0",
                                      method: String,
                                      params: Params,
                                      id: Option[Int])
  object SubscribeAuthParams {
    def create(apiAuth: ApiAuth): SubscribeAuthParams = {
      val timestamp = java.time.Instant.now().toEpochMilli
      val nonce = java.util.UUID.randomUUID.toString.replaceAll("-", "")
      val signature =
        SubscribeAuthParams.HMACSHA256(s"$timestamp$nonce", apiAuth.secret)
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
  case class SubscribeAuthParams(api_key: String,
                                 timestamp: Long,
                                 nonce: String,
                                 signature: String)

  sealed class BitflyerChannel(val channel: String, val idOption: Option[Int])
      extends Channel
  case class BTCJPYExecutions(id: Option[Int] = None)
      extends BitflyerChannel("lightning_executions_BTC_JPY", id)
  case class FXBTCJPYExecutions(id: Option[Int] = None)
      extends BitflyerChannel("lightning_executions_FX_BTC_JPY", id)
  case class BTCJPYBoard(id: Option[Int] = None)
      extends BitflyerChannel("lightning_board_BTC_JPY", id)
  case class FXBTCJPYBoard(id: Option[Int] = None)
      extends BitflyerChannel("lightning_board_FX_BTC_JPY", id)
  case class BTCJPYBoardSnapshot(id: Option[Int] = None)
      extends BitflyerChannel("lightning_board_snapshot_BTC_JPY", id)
  case class FXBTCJPYBoardSnapshot(id: Option[Int] = None)
      extends BitflyerChannel("lightning_board_snapshot_FX_BTC_JPY", id)

  case class ChildOrderEvents(id: Option[Int] = None)
      extends BitflyerChannel("child_order_events", id)
  case class ParentOrderEvents(id: Option[Int] = None)
      extends BitflyerChannel("parent_order_events", id)

  implicit val encodeUser: Encoder[BitflyerChannel] =
    Encoder.forProduct1("channel")(_.channel)

  trait JsonRpc extends ParsedJsonResponse {
    val jsonrpc: String = "2.0"
  }
  trait JsonRpcId extends JsonRpc {
    val id: Option[Int] = None
  }
  case class SignatureResult(override val id: Option[Int], result: Boolean)
      extends JsonRpcId
  case class ReceivedChannelMessage[Params](method: String, params: Params)
      extends JsonRpc

  case class ExecutionsChannelParams(channel: String,
                                     message: List[ExecutionsData])
  case class OrderEventsChannelParams(channel: String,
                                      message: List[ChildOrderEventData])

  type JsonrpcEvent = SignatureResult :+: ReceivedChannelMessage[
    ExecutionsChannelParams] :+: ReceivedChannelMessage[
    OrderEventsChannelParams] :+: CNil

}
