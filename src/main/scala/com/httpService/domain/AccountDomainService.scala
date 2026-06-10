package com.httpService.domain

import com.httpService.domain.Models.{Account, DomainError, Money}

/** Pure domain logic. No IO, no side effects. */
object AccountDomainService {

  /**
   * Returns [[DomainError.InsufficientFunds]] if balance would go negative.
   */
  def debit(account: Account, amount: Money): Either[DomainError, Account] =
    account.balance subtract amount match
      case Left(_) => Left(DomainError.InsufficientFunds(amount.value))
      case Right(newBalance) => Right(account.copy(balance = newBalance))

  /**
   * Always succeeds. Returns `Either` only for call-site uniformity with [[debit]].
   */
  def credit(account: Account, amount: Money): Either[DomainError, Account] =
    Right(account.copy(balance = account.balance add amount))
}
