package com.httpService.http

import io.circe.Codec

enum Request derives Codec.AsObject:
  case CreateAccountRequest(id: String, balance: BigDecimal, overdraftLimit: BigDecimal = 0)
  case DebitRequest(amount: BigDecimal)
  case CreditRequest(amount: BigDecimal)
  
  
