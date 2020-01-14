package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.BitflyerEnumDefinition.{OrderType, ProductCode, Side}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import shapeless._
import io.circe._

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

  sealed class Channel(val channel: String)
  case object BTCJPYExecutions extends Channel("lightning_executions_BTC_JPY")
  case object FXBTCJPYExecutions
      extends Channel("lightning_executions_FX_BTC_JPY")
  case object BTCJPYBoard extends Channel("lightning_board_BTC_JPY")
  case object FXBTCJPYBoard extends Channel("lightning_board_FX_BTC_JPY")
  case object BTCJPYBoardSnapshot
      extends Channel("lightning_board_snapshot_BTC_JPY")
  case object FXBTCJPYBoardSnapshot
      extends Channel("lightning_board_snapshot_FX_BTC_JPY")

  case object ChildOrderEvents extends Channel("child_order_events")
  case object ParentOrderEvents extends Channel("parent_order_events")

  implicit val encodeUser: Encoder[Channel] =
    Encoder.forProduct1("channel")(_.channel)


  trait JsonRpc {
    val jsonrpc: String = "2.0"
  }
  trait JsonRpcId extends JsonRpc {
    val id: Option[Int] = None
  }
  case object ConnectionOpened extends JsonRpc
  case class SignatureResult(result: Boolean) extends JsonRpcId
  case class ReceivedChannelMessage[Params](method: String, params: Params) extends JsonRpc
  case class ParseError(message: String) extends JsonRpc

  case class ExecutionsChannelParams(channel: String, message: List[ExecutionsData])
  case class ExecutionsData(id: Long, side: Side, price: Long, size: BigDecimal, exec_date: String, buy_child_order_acceptance_id: String, sell_child_order_acceptance_id: String) {
    val delayEpochMilli: Long = java.time.Instant.now().toEpochMilli - java.time.Instant.parse(exec_date).toEpochMilli
  }

  case class OrderEventsChannelParams(channel: String, message: List[ChildOrderEventData])
  case class ChildOrderEventData(product_code: ProductCode, child_order_id: String, child_order_acceptance_id: String, event_date: String, event_type: String, child_order_type: OrderType, side: Side, price: Long, size: BigDecimal, expire_date: String)

  type JsonrpcEvent = SignatureResult :+: ReceivedChannelMessage[ExecutionsChannelParams] :+: ReceivedChannelMessage[OrderEventsChannelParams] :+: CNil
}
