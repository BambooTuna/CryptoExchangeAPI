package com.github.BambooTuna.CryptoExchangeAPI.bitmex.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.bitmex.protocol.core.BitmexEnumDefinition._

case class TradeData(timestamp: String,
                     symbol: ProductCode,
                     side: Side,
                     size: BigDecimal,
                     price: BigDecimal,
                     tickDirection: TickDirection,
                     trdMatchID: String,
                     grossValue: Long,
                     homeNotional: BigDecimal,
                     foreignNotional: Int)
