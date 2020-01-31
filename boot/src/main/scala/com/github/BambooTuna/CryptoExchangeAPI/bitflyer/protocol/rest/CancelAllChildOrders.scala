package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.ProductCode

object CancelAllChildOrders {
  case class Request(
      product_code: ProductCode = ProductCode.FXBTCJPY
  )
}
