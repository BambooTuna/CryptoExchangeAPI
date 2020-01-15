package com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

object BitflyerEnumDefinition {

  sealed abstract class ProductCode(val value: String) extends StringEnumEntry
  case object ProductCode
      extends StringEnum[ProductCode]
      with StringCirceEnum[ProductCode] {

    case object BTCJPY extends ProductCode("BTC_JPY")
    case object FXBTCJPY extends ProductCode("FX_BTC_JPY")

    val values = findValues
  }

  sealed abstract class OrderType(val value: String) extends StringEnumEntry
  case object OrderType
      extends StringEnum[OrderType]
      with StringCirceEnum[OrderType] {

    case object Limit extends OrderType("LIMIT")
    case object Market extends OrderType("MARKET")

    val values = findValues
  }

  sealed abstract class OrderEventType(val value: String)
      extends StringEnumEntry
  case object OrderEventType
      extends StringEnum[OrderEventType]
      with StringCirceEnum[OrderEventType] {

    case object ORDER extends OrderEventType("ORDER")
    case object ORDER_FAILED extends OrderEventType("ORDER_FAILED")
    case object CANCEL extends OrderEventType("CANCEL")
    case object CANCEL_FAILED extends OrderEventType("CANCEL_FAILED")
    case object EXECUTION extends OrderEventType("EXECUTION")
    case object EXPIRE extends OrderEventType("EXPIRE")

    val values = findValues
  }

  sealed abstract class Side(val value: String) extends StringEnumEntry
  case object Side extends StringEnum[Side] with StringCirceEnum[Side] {

    case object Buy extends Side("BUY")
    case object Sell extends Side("SELL")

    val values = findValues
  }

  sealed abstract class TimeInForce(val value: String) extends StringEnumEntry
  case object TimeInForce
      extends StringEnum[TimeInForce]
      with StringCirceEnum[TimeInForce] {

    case object GTC extends TimeInForce("GTC")
    case object IOC extends TimeInForce("IOC")
    case object FOK extends TimeInForce("FOK")

    val values = findValues
  }

  sealed abstract class OrderStatus(val value: String) extends StringEnumEntry
  case object OrderStatus
      extends StringEnum[OrderStatus]
      with StringCirceEnum[OrderStatus] {

    case object ACTIVE extends OrderStatus("ACTIVE")
    case object COMPLETED extends OrderStatus("COMPLETED")
    case object CANCELED extends OrderStatus("CANCELED")
    case object EXPIRED extends OrderStatus("EXPIRED")
    case object REJECTED extends OrderStatus("REJECTED")
    case object Empty extends OrderStatus("")

    val values = findValues
  }

  sealed abstract class PositionStatus(val value: String)
      extends StringEnumEntry
  case object PositionStatus
      extends StringEnum[PositionStatus]
      with StringCirceEnum[PositionStatus] {

    case object Open extends PositionStatus("open")
    case object Closed extends PositionStatus("closed")
    case object Empty extends PositionStatus("")

    val values = findValues
  }

}
