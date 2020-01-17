package com.github.BambooTuna.CryptoExchangeAPI.core.http

case class HttpInterpreterResponse[T](statusCode: Int, body: T)
