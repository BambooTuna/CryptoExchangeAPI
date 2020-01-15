package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.Side

case class ExecutionsData(id: Long,
                          side: Side,
                          price: Long,
                          size: BigDecimal,
                          exec_date: String,
                          buy_child_order_acceptance_id: String,
                          sell_child_order_acceptance_id: String) {
  val delayEpochMilli: Long = java.time.Instant
    .now()
    .toEpochMilli - java.time.Instant.parse(exec_date).toEpochMilli
}
