package com.httpService.domain

import com.httpService.domain.Models.AccountId.AccountId

object Models {

  object AccountId {
    opaque type AccountId = String

    def from(value: String): Either[String, AccountId] =
      if (value.trim.isEmpty) Left("AccountId cannot be empty")
      else Right(value)

    extension (id: AccountId)
      def value: String = id
  }

  opaque type Money = BigDecimal
  object Money {

    def from(value: BigDecimal): Either[String, Money] =
      if (value <= 0) Left("Money must be positive")
      else Right(value)

    extension (m: Money)

      def add(other: Money): Money = m + other
      def subtract(other: Money): Money = m - other
      def lessThen(other: Money): Boolean = m < other
  }

  opaque type Balance = BigDecimal
  object Balance {

    def from(value: BigDecimal): Either[String, Balance] =
      if (value < 0) Left("Balance cannot be negative")
      else Right(value)

    extension (b: Balance)

      def add(m: Money): Balance = b + m
      def subtract(m: Money): Either[DomainError, Balance] =
        val result = b - m
        if (result < 0) Left(DomainError.InsufficientFunds)
        else Right(result)

      def lessThen(m: Money): Boolean = b < m

      def value: BigDecimal = b
  }

  final case class Account(
                            id: AccountId,
                            balance: Balance
                          )

  sealed trait DomainError
  object DomainError {
    case object AccountNotFound extends DomainError

    case object InsufficientFunds extends DomainError

    case object InvalidAmount extends DomainError

    case object AccountAlreadyExists extends DomainError
  }
}
