package com.httpService.http

import com.httpService.domain.Models.Account

/**
 * Stable HTTP response shape, decoupled from the internal domain model.
 * Unwraps opaque types to primitives.
 */
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