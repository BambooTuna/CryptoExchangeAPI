package com.github.BambooTuna.CryptoExchangeAPI.bitmex

import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth
import com.github.BambooTuna.CryptoExchangeAPI.core.rest.ExchangeRestAPI

import io.circe.generic.auto._

class BitmexRestAPI(apiAuth: ApiAuth) extends BitmexRestAPICore {

}

object BitmexRestAPI {
  implicit val bitmexRestAPI = new ExchangeRestAPI[BitmexRestAPI] {
    def generate(apiAuth: ApiAuth): BitmexRestAPI = new BitmexRestAPI(apiAuth)
  }
}
