package com.httpService.http

final case class DebitRequest(amount: BigDecimal)

final case class CreditRequest(amount: BigDecimal)
