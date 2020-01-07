package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.BambooTuna.WebSocketManager.WebSocketProtocol.{
  ConnectStart,
  ConnectedSucceeded,
  OnMessage,
  Receive,
  SendMessage
}
import com.github.BambooTuna.WebSocketManager.{
  WebSocketManager,
  WebSocketOptions
}

class BitflyerRealtimeAPI(implicit system: ActorSystem,
                          materializer: Materializer) {

//  import akka.util.Timeout
//  import scala.concurrent.duration._
//  implicit val timeout = Timeout(5.seconds)
//
//  val subscribeMessage =
//    """{"method":"subscribe","params":{"channel":"lightning_executions_FX_BTC_JPY"}}"""
//  val webSocketManager = system.actorOf(
//    Props(
//      classOf[WebSocketManager],
//      WebSocketOptions(host = "wss://ws.lightstream.bitflyer.com/json-rpc")),
//    WebSocketManager.ActorName)
//
//  val flow = Flow[Receive].ask(parallelism = 5)(webSocketManager)
//  val simpleSink = Sink.foreach {
//    case ConnectedSucceeded(ws) => ws ! SendMessage(subscribeMessage)
//    case OnMessage(m)           => println(m)
//    case a                      => println(a)
//  }
//
//  Source(List(ConnectStart)).via(flow).to(simpleSink).run()

}
