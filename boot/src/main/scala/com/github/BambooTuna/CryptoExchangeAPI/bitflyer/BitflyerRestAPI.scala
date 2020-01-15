package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import cats.Id
import cats.data.{EitherT, Kleisli}
import com.github.BambooTuna.CryptoExchangeAPI.bitflyer.protocol.rest.SendChildOrder
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.http.{
  HttpInternalException,
  HttpInterpreter
}
import io.circe.syntax._
import io.circe.generic.auto._
import monix.eval.Task

class BitflyerRestAPI {
  type Res[O] = Kleisli[Id, ApiAuth, EitherT[Task, HttpInternalException, O]]
  private val api = new BitflyerAPICommand

  def childOrder(entity: SendChildOrder.Request)(
      implicit system: ActorSystem,
      materializer: Materializer): Res[SendChildOrder.Response] =
    api
      .generate(method = HttpMethods.POST,
                path = "/v1/me/sendchildorder",
                entity = entity.asJson.noSpaces)
      .map(HttpInterpreter.runRequest)

}
