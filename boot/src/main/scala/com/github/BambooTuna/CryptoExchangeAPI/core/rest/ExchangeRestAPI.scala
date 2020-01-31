package com.github.BambooTuna.CryptoExchangeAPI.core.rest

import com.github.BambooTuna.CryptoExchangeAPI.core.domain.ApiAuth

trait ExchangeRestAPI[T <: Exchange] {
  def generate(apiAuth: ApiAuth): T
}

object ExchangeRestAPI {
  def generate[T <: Exchange](apiAuth: ApiAuth)(
      implicit exchangeAPI: ExchangeRestAPI[T]): T =
    exchangeAPI.generate(apiAuth)
}
