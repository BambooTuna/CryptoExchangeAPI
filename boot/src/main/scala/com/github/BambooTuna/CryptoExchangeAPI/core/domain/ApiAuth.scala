package com.github.BambooTuna.CryptoExchangeAPI.core.domain

case class ApiAuth(key: String, secret: String) {
  require(key.nonEmpty && secret.nonEmpty, "key or secret is empty!")
}
