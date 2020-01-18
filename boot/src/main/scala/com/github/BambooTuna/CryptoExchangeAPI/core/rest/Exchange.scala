package com.github.BambooTuna.CryptoExchangeAPI.core.rest

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import cats.Id
import cats.data.{EitherT, Kleisli, Reader}
import com.github.BambooTuna.CryptoExchangeAPI.core.domain.{ApiAuth, EndPoint}
import com.github.BambooTuna.CryptoExchangeAPI.core.http.{HttpInternalException, HttpInterpreterResponse}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import monix.eval.Task

trait Exchange {
  type Res[O] = Kleisli[Id, ApiAuth, EitherT[Task, HttpInternalException, HttpInterpreterResponse[O]]]

  protected val endPoint: EndPoint

  protected def generate(
                method: HttpMethod,
                path: String,
                queryString: Option[String] = None,
                entity: Option[String] = None,
                headers: Map[String, String] = Map.empty): Reader[ApiAuth, HttpRequest]

  protected def toRawHeaders(headers: Map[String, String]): List[RawHeader] =
    headers.toList.map(h => RawHeader(h._1, h._2))

  protected def HMACSHA256(text: String, secret: String): String = {
    val algorithm = "HMacSha256"
    val secretKey = new SecretKeySpec(secret.getBytes, algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secretKey)
    mac
      .doFinal(text.getBytes)
      .foldLeft("")((l, r) =>
        l + Integer.toString((r & 0xff) + 0x100, 16).substring(1))
  }

}
