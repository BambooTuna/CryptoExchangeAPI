package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.{ProductCode, Side}

object GetPositions {
  case class Request(product_code: ProductCode = ProductCode.FXBTCJPY)

  case class Response(
                       product_code: ProductCode,
                       side: Side,
                       price: Long,
                       size: BigDecimal,
                       commission: BigDecimal,
                       swap_point_accumulate: BigDecimal,
                       require_collateral: BigDecimal,
                       open_date: String,
                       leverage: Int,
                       pnl: BigDecimal,
                       sfd: BigDecimal
                     )

}
