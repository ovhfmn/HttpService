package com.httpService.http

enum Request:
  case CreateAccountRequest(id: String, balance: BigDecimal, overdraftLimit: BigDecimal = 0)
  case DebitRequest(amount: BigDecimal)
  case CreditRequest(amount: BigDecimal)
