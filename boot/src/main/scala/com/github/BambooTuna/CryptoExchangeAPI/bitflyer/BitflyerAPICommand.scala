package com.github.BambooTuna.CryptoExchangeAPI.bitflyer

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethod, HttpRequest, Uri}
import cats.data.Reader
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.{ApiAuth, EndPoint}
import com.github.BambooTuna.CryptoExchangeAPI.core.http.APICommand

class BitflyerAPICommand extends APICommand {
  val endPoint: EndPoint =
    EndPoint(scheme = "https", host = "api.bitflyer.com", port = 443)

  def generate(
      method: HttpMethod,
      path: String,
      queryString: Option[String] = None,
      entity: Option[String] = None,
      headers: Map[String, String] = Map.empty): Reader[ApiAuth, HttpRequest] =
    Reader[ApiAuth, HttpRequest] { implicit apiAuth: ApiAuth =>
      implicit val uri: Uri = endPoint.uri(path, queryString)
      HttpRequest(
        method = method,
        uri = uri,
        headers = toRawHeaders(authHeaderMap(method, uri, entity.getOrElse(""))) ++ toRawHeaders(
          headers),
        entity = entity.fold(HttpEntity.Empty)(e => HttpEntity(ContentTypes.`application/json`, e))
      )
    }

  override def authHeaderMap(method: HttpMethod, uri: Uri, entity: String)(
      implicit apiAuth: ApiAuth): Map[String, String] = {
    val timestamp = java.time.Instant.now().toEpochMilli.toString
    val text = timestamp + method.value + uri.path
      .toString() + uri.rawQueryString.map("?" + _).getOrElse("") + entity
    Map(
      "ACCESS-KEY" -> apiAuth.key,
      "ACCESS-TIMESTAMP" -> timestamp,
      "ACCESS-SIGN" -> HMACSHA256(text, apiAuth.secret)
    )
  }

}
