package com.httpService.http

import io.circe.Codec

enum Request derives Codec.AsObject:
  case CreateAccountRequest(id: String, balance: BigDecimal)
  case DebitRequest(amount: BigDecimal)
  case CreditRequest(amount: BigDecimal)
  
  
