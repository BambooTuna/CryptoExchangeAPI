package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.BitflyerEnumDefinition.{
  OrderType,
  ProductCode,
  Side,
  TimeInForce
}

object SendChildOrder {
  case class Request(
      product_code: ProductCode = ProductCode.FXBTCJPY,
      child_order_type: OrderType,
      side: Side,
      price: Long,
      size: BigDecimal,
      minute_to_expire: Long = 43200,
      time_in_force: TimeInForce = TimeInForce.GTC
  )
  case class Response(child_order_acceptance_id: String)
}
