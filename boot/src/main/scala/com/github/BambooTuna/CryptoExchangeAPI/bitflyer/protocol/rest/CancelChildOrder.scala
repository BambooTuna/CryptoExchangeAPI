package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.ProductCode

object CancelChildOrder {
  case class Request(
                      product_code: ProductCode,
                      child_order_acceptance_id: String
                    )
}
