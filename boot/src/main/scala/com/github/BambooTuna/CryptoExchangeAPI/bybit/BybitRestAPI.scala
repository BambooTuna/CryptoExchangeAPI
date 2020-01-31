package com.github.BambooTuna.CryptoExchangeAPI.bybit

import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.rest.ExchangeRestAPI

import io.circe.generic.auto._

class BybitRestAPI(apiAuth: ApiAuth) extends BybitRestAPICore {}

object BybitRestAPI {
  implicit val bybitRestAPI = new ExchangeRestAPI[BybitRestAPI] {
    def generate(apiAuth: ApiAuth): BybitRestAPI = new BybitRestAPI(apiAuth)
  }
}
