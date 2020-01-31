package com.github.BambooTuna.CryptoExchangeAPI.bybit.protocol.core

import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.ProductCode.findValues
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.Side.findValues
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

object BybitEnumDefinition {

  sealed abstract class ProductCode(val value: String) extends StringEnumEntry
  case object ProductCode
      extends StringEnum[ProductCode]
      with StringCirceEnum[ProductCode] {

    case object BTCUSD extends ProductCode("BTCUSD")

    val values = findValues
  }

  sealed abstract class Side(val value: String) extends StringEnumEntry
  case object Side extends StringEnum[Side] with StringCirceEnum[Side] {

    case object Buy extends Side("Buy")
    case object Sell extends Side("Sell")

    val values = findValues
  }

  sealed abstract class TickDirection(val value: String) extends StringEnumEntry
  case object TickDirection
      extends StringEnum[TickDirection]
      with StringCirceEnum[TickDirection] {

    case object PlusTick extends TickDirection("PlusTick")
    case object ZeroPlusTick extends TickDirection("ZeroPlusTick")
    case object ZeroMinusTick extends TickDirection("ZeroMinusTick")
    case object MinusTick extends TickDirection("MinusTick")

    val values = findValues
  }

}
