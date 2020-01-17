package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import cats.Id
import cats.data.{EitherT, Kleisli}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.core.BitflyerEnumDefinition.ProductCode
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest.{GetChildOrders, SendChildOrder, _}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.http._
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import monix.eval.Task

class BitflyerRestAPI {
  type Res[O] = Kleisli[Id, ApiAuth, EitherT[Task, HttpInternalException, HttpInterpreterResponse[O]]]
  private val api = new BitflyerAPICommand

  def childOrder(entity: SendChildOrder.Request)(
      implicit system: ActorSystem,
      materializer: Materializer): Res[SendChildOrder.Response] =
    api
      .generate(method = HttpMethods.POST,
                path = "/v1/me/sendchildorder",
                entity = encodeRequestBody(entity)
      )
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.parseResponse))

  def cancelChildOrder(child_order_acceptance_id: String, product_code: ProductCode = ProductCode.FXBTCJPY)(
    implicit system: ActorSystem,
    materializer: Materializer): Res[String] =
    api
      .generate(method = HttpMethods.POST,
        path = "/v1/me/cancelchildorder",
        entity = encodeRequestBody(CancelChildOrder.Request(product_code, child_order_acceptance_id))
      )
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.checkStatusCode()))

  def cancelAllChildOrders(product_code: ProductCode = ProductCode.FXBTCJPY)(
    implicit system: ActorSystem,
    materializer: Materializer): Res[String] =
    api
      .generate(method = HttpMethods.POST,
        path = "/v1/me/cancelallchildorders",
        entity = encodeRequestBody(CancelAllChildOrders.Request(product_code))
      )
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.checkStatusCode()))

  def getChildOrders(getChildOrders: GetChildOrders.Request)(implicit system: ActorSystem, materializer: Materializer): Res[Seq[GetChildOrders.Response]] =
    api
      .generate(method = HttpMethods.GET,
        path = "/v1/me/getchildorders",
        queryString = encodeQueryParameter(getChildOrders))
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.parseResponse))

  def getPositions(getPositions: GetPositions.Request)(implicit system: ActorSystem, materializer: Materializer): Res[Seq[GetPositions.Response]] =
    api
      .generate(method = HttpMethods.GET,
        path = "/v1/me/getpositions",
        queryString = encodeQueryParameter(getPositions))
      .map(HttpInterpreter.runRequest(_)(HttpInterpreter.parseResponse))

  private def encodeRequestBody[T](requestBody: T)(implicit encoder: Encoder[T]): Option[String] =
    Some(requestBody.asJson.noSpaces)

  private def encodeQueryParameter[T](queryParameter: T)(implicit encoder: Encoder[T]): Option[String] = {
    queryParameter.asJson.asObject.map(
      _.toList
        .collect {
          case (str, json) if !json.isNull => s"${str}=${json.toString().replace("\"", "")}"
        }
        .reduce(_ + "&" + _)
    )
  }
}
