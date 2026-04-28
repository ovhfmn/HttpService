package com.httpService.http

import io.circe.Codec

enum AccountEvent derives Codec.AsObject:
  case AccountCreatedEvent(id: String, initialBalance: BigDecimal)
  case MoneyDebitedEvent(id: String, amount: BigDecimal, newBalance: BigDecimal)
  case MoneyCreditedEvent(id: String, amount: BigDecimal, newBalance: BigDecimal)