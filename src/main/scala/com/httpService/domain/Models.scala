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
      def value: BigDecimal = m
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
        if (result < 0) Left(DomainError.InsufficientFunds(m.value))
        else Right(result)

      def lessThen(m: Money): Boolean = b < m

      def value: BigDecimal = b
  }

  final case class Account(
                            id: AccountId,
                            balance: Balance,
                            version: Long = 0
                          )

  sealed trait DomainError
  object DomainError {
    final case class AccountNotFound(id: String) extends DomainError

    final case class InvalidAccountId(id: String) extends DomainError

    final case class InsufficientFunds(requested: BigDecimal) extends DomainError

    final case class AccountAlreadyExists(id: String) extends DomainError

    final case class InvalidAmount(value: BigDecimal) extends DomainError

    final case class TechnicalFailure(msg: String) extends DomainError
    
    final case class ConcurrentModification(id: String) extends DomainError
  }
}
