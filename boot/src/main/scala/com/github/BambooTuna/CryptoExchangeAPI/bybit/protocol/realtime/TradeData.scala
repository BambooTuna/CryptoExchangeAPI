package com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.core.BybitEnumDefinition.{
  ProductCode,
  Side,
  TickDirection
}

case class TradeData(timestamp: String,
                     symbol: ProductCode,
                     side: Side,
                     size: BigDecimal,
                     price: BigDecimal,
                     tick_direction: TickDirection,
                     trade_id: String,
                     cross_seq: Long)
