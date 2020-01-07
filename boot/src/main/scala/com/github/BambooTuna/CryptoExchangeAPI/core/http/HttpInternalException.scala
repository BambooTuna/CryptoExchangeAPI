package com.github.BambooTuna.CryptoExchangeAPI.core.http

sealed trait HttpInternalException {
  val message: String
}

case class FetchOriginEntityDataException(message: String)
    extends HttpInternalException
case class DecodeResponseBodyException(message: String,
                                       statusCode: Int,
                                       body: Option[String] = None)
    extends HttpInternalException
case class UnmarshalToStringException(message: String,
                                      statusCode: Int,
                                      body: Option[String] = None)
    extends HttpInternalException
case class HttpSingleRequestException(message: String)
    extends HttpInternalException
