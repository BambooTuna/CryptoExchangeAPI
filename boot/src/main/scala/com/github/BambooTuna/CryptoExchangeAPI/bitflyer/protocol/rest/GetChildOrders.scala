package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.{
  OrderStatus,
  OrderType,
  ProductCode,
  Side
}

object GetChildOrders {
  case class Request(
      product_code: ProductCode = ProductCode.FXBTCJPY,
      count: Option[Int] = None,
      before: Option[Int] = None,
      after: Option[Int] = None,
      child_order_state: OrderStatus = OrderStatus.ACTIVE,
      child_order_id: Option[String] = None,
      child_order_acceptance_id: Option[String] = None,
      parent_order_id: Option[String] = None
  )

  case class Response(
      id: Long,
      child_order_id: String,
      product_code: String,
      side: Side,
      child_order_type: OrderType,
      price: Long,
      average_price: BigDecimal,
      size: BigDecimal,
      child_order_state: OrderStatus,
      expire_date: String,
      child_order_date: String,
      child_order_acceptance_id: String,
      outstanding_size: BigDecimal,
      cancel_size: BigDecimal,
      executed_size: BigDecimal,
      total_commission: BigDecimal
  )

}
