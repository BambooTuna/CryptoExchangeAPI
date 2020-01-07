package com.github.BambooTuna.CryptoExchangeAPI.core.http

import akka.http.scaladsl.model.{HttpMethod, Uri}
import akka.http.scaladsl.model.headers.RawHeader
import com.github.BambooTuna.CryptoExchangeAPI.core.domain._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

trait APICommand {
  val endPoint: EndPoint

  protected def authHeaderMap(method: HttpMethod, uri: Uri, entity: String)(
      implicit apiAuth: ApiAuth): Map[String, String]

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
