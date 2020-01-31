package com.github.BambooTuna.CryptoExchangeAPI.bitmex

import akka.http.scaladsl.model._
import cats.data.Reader
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.{ApiAuth, EndPoint}
import com.github.BambooTuna.CryptoExchangeAPI.core.rest.Exchange

trait BitmexRestAPICore extends Exchange {
  override val endPoint: EndPoint =
    EndPoint(scheme = "https", host = "api.bitflyer.com", port = 443)

  override def generate(
      method: HttpMethod,
      path: String,
      queryString: Option[String] = None,
      entity: Option[String] = None,
      headers: Map[String, String] = Map.empty): Reader[ApiAuth, HttpRequest] =
    ???

}
