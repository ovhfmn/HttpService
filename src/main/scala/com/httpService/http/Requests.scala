package com.httpService.http


object Requests {

  final case class DebitRequest(amount: BigDecimal)

  final case class CreditRequest(amount: BigDecimal)

}