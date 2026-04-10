package com.httpService.http

import com.httpService.domain.domain.Account

final case class AccountResponse(
                                  id: String,
                                  balance: BigDecimal
                                )

object AccountResponse {

  def from(account: Account): AccountResponse =
    AccountResponse(
      id = account.id.value,
      balance = account.balance.value
    )
}