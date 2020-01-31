package com.github.BambooTuna.CryptoExchangeAPI.core.http

sealed trait HttpInternalException {
  val errorMessage: String
}

case class UnmarshalToStringException(statusCode: Int, errorMessage: String)
    extends HttpInternalException

case class HttpSingleRequestException(errorMessage: String)
    extends HttpInternalException

case class DecodeResponseBodyException(statusCode: Int,
                                       originBody: String,
                                       errorMessage: String)
    extends HttpInternalException

case class BadResponseStatusCodeException(statusCode: Int,
                                          originBody: String,
                                          errorMessage: String)
    extends HttpInternalException
