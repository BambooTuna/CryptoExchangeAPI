package com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.realtime

import com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.core.BybitEnumDefinition.{
  ProductCode,
  Side
}

case class OrderBooks(delete: Seq[OrderBookData],
                      update: Seq[OrderBookData],
                      insert: Seq[OrderBookData])

case class OrderBookData(price: BigDecimal,
                         symbol: ProductCode,
                         id: Long,
                         side: Side,
                         size: Option[BigDecimal])
