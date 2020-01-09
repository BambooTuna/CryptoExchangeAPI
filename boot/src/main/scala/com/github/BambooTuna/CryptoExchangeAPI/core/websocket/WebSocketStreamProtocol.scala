package com.github.BambooTuna.CryptoExchangeAPI.core.websocket

import akka.http.scaladsl.model.ws.Message

import scala.concurrent.Future

object WebSocketStreamProtocol {

  sealed trait InternalFlowObject
  case class ReceivedMessage(message: Message) extends InternalFlowObject
  case class ParsedMessage(value: String) extends InternalFlowObject
  case class SendMessage(value: String) extends InternalFlowObject
  case class ConnectionOpenedFuture(value: Future[InternalFlowObject])
      extends InternalFlowObject
  case object ConnectionOpened extends InternalFlowObject
  case class InternalException(value: String) extends InternalFlowObject
  case object NotUsedObject extends InternalFlowObject

}
