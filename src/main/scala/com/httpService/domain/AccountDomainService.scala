package com.httpService.domain

import com.httpService.domain.*
import com.httpService.domain.Models.{Account, DomainError, Money}

object AccountDomainService {

  def debit(account: Account, amount: Money): Either[DomainError, Account] =
    if (account.balance.lessThen(amount))
      Left(DomainError.InsufficientFunds(amount.value))
    else
      account.balance subtract amount match
        case Left(_) => Left(DomainError.InsufficientFunds(amount.value))
        case Right(newBalance) =>
          Right(account.copy(balance = newBalance))

  def credit(account: Account, amount: Money): Either[DomainError, Account] =
    Right(account.copy(balance = account.balance add amount))
}
