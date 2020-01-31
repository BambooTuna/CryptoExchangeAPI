package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.{
  OrderEventType,
  OrderType,
  ProductCode,
  Side
}

case class ChildOrderEventData(product_code: ProductCode,
                               child_order_id: String,
                               child_order_acceptance_id: String,
                               event_date: String,
                               event_type: OrderEventType,
                               child_order_type: Option[OrderType],
                               side: Option[Side],
                               price: Option[Long],
                               size: Option[BigDecimal],
                               expire_date: Option[String],
                               reason: Option[String],
                               exec_id: Option[Long],
                               commission: Option[BigDecimal],
                               sfd: Option[BigDecimal]) {
  require(
    (event_type == OrderEventType.ORDER && child_order_type.isDefined && expire_date.isDefined && side.isDefined && price.isDefined && size.isDefined) ||
      (event_type == OrderEventType.EXECUTION && exec_id.isDefined && side.isDefined && price.isDefined && size.isDefined && commission.isDefined && sfd.isDefined) ||
      (event_type == OrderEventType.ORDER_FAILED && reason.isDefined) ||
      event_type == OrderEventType.CANCEL ||
      event_type == OrderEventType.CANCEL_FAILED ||
      event_type == OrderEventType.EXPIRE
  )
}
