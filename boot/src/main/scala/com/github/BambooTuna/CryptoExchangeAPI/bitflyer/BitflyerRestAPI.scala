package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.ProductCode
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest.{
  CancelAllChildOrders,
  CancelChildOrder,
  GetChildOrders,
  GetPositions,
  SendChildOrder
}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.http.HttpInterpreter
import com.github.BambooTuna.CryptoExchangeAPI.core.rest.ExchangeRestAPI

import io.circe.generic.auto._

class BitflyerRestAPI(apiAuth: ApiAuth) extends BitflyerRestAPICore {

  def childOrder(entity: SendChildOrder.Request)(
      implicit system: ActorSystem,
      materializer: Materializer): Res[SendChildOrder.Response] =
    generate(method = HttpMethods.POST,
             path = "/v1/me/sendchildorder",
             entity = encodeRequestBody(entity))
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.parseResponse))

  def cancelChildOrder(child_order_acceptance_id: String,
                       product_code: ProductCode = ProductCode.FXBTCJPY)(
      implicit system: ActorSystem,
      materializer: Materializer): Res[String] =
    generate(
      method = HttpMethods.POST,
      path = "/v1/me/cancelchildorder",
      entity = encodeRequestBody(
        CancelChildOrder.Request(product_code, child_order_acceptance_id)))
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.checkStatusCode()))

  def cancelAllChildOrders(product_code: ProductCode = ProductCode.FXBTCJPY)(
      implicit system: ActorSystem,
      materializer: Materializer): Res[String] =
    generate(method = HttpMethods.POST,
             path = "/v1/me/cancelallchildorders",
             entity =
               encodeRequestBody(CancelAllChildOrders.Request(product_code)))
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.checkStatusCode()))

  def getChildOrders(getChildOrders: GetChildOrders.Request)(
      implicit system: ActorSystem,
      materializer: Materializer): Res[Seq[GetChildOrders.Response]] =
    generate(method = HttpMethods.GET,
             path = "/v1/me/getchildorders",
             queryString = encodeQueryParameter(getChildOrders))
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.parseResponse))

  def getPositions(getPositions: GetPositions.Request)(
      implicit system: ActorSystem,
      materializer: Materializer): Res[Seq[GetPositions.Response]] =
    generate(method = HttpMethods.GET,
             path = "/v1/me/getpositions",
             queryString = encodeQueryParameter(getPositions))
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.parseResponse))

}

object BitflyerRestAPI {
  implicit val bitflyerRestAPI = new ExchangeRestAPI[BitflyerRestAPI] {
    def generate(apiAuth: ApiAuth): BitflyerRestAPI =
      new BitflyerRestAPI(apiAuth)
  }
}
