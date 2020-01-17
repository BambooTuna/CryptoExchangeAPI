package com.github.BambooTuna.CryptoExchangeAPI

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer._
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.{OrderType, Side}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime.BitflyerRealtimeAPIProtocol._
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.realtime.BitflyerRealtimeAPIProtocol
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest._
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val apiKey = ???

  val restAPI = new BitflyerRestAPI()

  val task =
    for {
      order <- restAPI.childOrder(SendChildOrder.Request(child_order_type = OrderType.Limit, side = Side.Buy, price = 900001, size = 0.01)).run(apiKey)
      cancel <- restAPI.cancelChildOrder(order.body.child_order_acceptance_id).run(apiKey)
      getChildOrders <- restAPI.getChildOrders(GetChildOrders.Request()).run(apiKey)
      getPositions <- restAPI.getPositions(GetPositions.Request()).run(apiKey)
      cancelAllChildOrders <- restAPI.cancelAllChildOrders().run(apiKey)
    } yield List(order, cancel, getChildOrders, getPositions, cancelAllChildOrders).map(_.body)

  task
    .value
    .runToFuture(monix.execution.Scheduler.Implicits.global)
    .foreach(println)



  val realtimeAPI = BitflyerRealtimeAPI()

  val parsedMessageSink = Sink.foreach[JsonRpc] {
    case BitflyerRealtimeAPIProtocol.ConnectionOpened =>
      println("ConnectionOpened")
      realtimeAPI.authMessage(apiKey, 1)
      realtimeAPI.subscribeMessage(
        (BTCJPYExecutions, None)
      )
    case a: SignatureResult =>
      if (a.result && a.id.contains(1)) {
        println("Auth Subscribe Success")
        realtimeAPI.subscribeMessage(
          (ChildOrderEvents, Some(2)),
          (ParentOrderEvents, Some(3)),
        )
      } else if (a.result) {
        println(s"Private Channel Subscribe Success | id: ${a.id}")
      } else {
        println("Subscribe false")
      }
    case a: ReceivedChannelMessage[_] =>
      a.params match {
        case p: ExecutionsChannelParams  => p.message.foreach(println)
        case p: OrderEventsChannelParams => p.message.foreach(println)
      }
    case ParseError(origin) =>
      println(s"ParseError origin: $origin")
    case other => println(other)
  }
//  realtimeAPI.runBySink(parsedMessageSink)

}
