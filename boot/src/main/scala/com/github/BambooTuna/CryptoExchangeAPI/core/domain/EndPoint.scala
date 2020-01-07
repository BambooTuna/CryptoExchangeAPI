package com.github.BambooTuna.CryptoExchangeAPI.core.domain

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path

case class EndPoint(scheme: String, host: String, port: Int) {
  val coreUri = Uri.from(scheme = scheme, host = host, port = port)

  def uri(path: String, queryString: Option[String]): Uri =
    queryString.fold(coreUri.withPath(Path(path)))(
      coreUri.withPath(Path(path)).withRawQueryString(_))
}
