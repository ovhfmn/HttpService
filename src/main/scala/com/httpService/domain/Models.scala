package com.httpService.domain

object Models {

  /**
   * Construction only through [[AccountId.from]], rejects blank strings.
   */
  opaque type AccountId = String
  object AccountId {

    def from(value: String): Either[String, AccountId] =
      if (value.trim.isEmpty) Left("AccountId cannot be empty")
      else Right(value)

    extension (id: AccountId)
      def value: String = id
  }


  /**
   * Strictly positive. Zero and negative values rejected by [[Money.from]].
   */
  opaque type Money = BigDecimal
  object Money {

    def from(value: BigDecimal): Either[String, Money] =
      if (value <= 0) Left("Money must be positive")
      else Right(value)

    extension (m: Money)

      def add(other: Money): Money = m + other
      def subtract(other: Money): Money = m - other
      def lessThan(other: Money): Boolean = m < other
      def value: BigDecimal = m
  }

  /**
   * Zero is valid (empty account).
   * Negative values rejected by [[Balance.from]].
   */
  opaque type Balance = BigDecimal
  object Balance {

    def from(value: BigDecimal): Either[String, Balance] =
      Right(value)

    extension (b: Balance)
      def add(m: Money): Balance = b + m

      def subtract(m: Money, limit: OverdraftLimit): Either[DomainError, Balance] =
        import com.httpService.domain.Models.OverdraftLimit.allow
        val result = b - m
        if (limit.allow(result)) Right(result)
        else Left(DomainError.InsufficientFunds(m.value))

      def lessThan(m: Money): Boolean = b < m
      def value: BigDecimal = b
  }

  /**
   * Zero is valid — accounts with no overdraft facility.
   * Negative values are not valid — an overdraft limit cannot be negative.
   */
  opaque type OverdraftLimit = BigDecimal
  object OverdraftLimit {
    val zero: OverdraftLimit = BigDecimal(0)

    def from(value: BigDecimal): Either[String, OverdraftLimit] =
      if (value < 0) Left("OverdraftLimit cannot be negative")
      else Right(value)

    extension (o: OverdraftLimit)
      def value: BigDecimal = o
      def allow(balance: BigDecimal): Boolean = balance >= -o
  }

  /**
   * @param version Optimistic-locking counter. Incremented on every successful update in the DB.
   */
  final case class Account(
                            id: AccountId,
                            balance: Balance,
                            overdraftLimit: OverdraftLimit = OverdraftLimit.zero,
                            version: Long = 0
                          )

  sealed trait DomainError
  object DomainError {
    final case class AccountNotFound(id: String) extends DomainError

    final case class InvalidAccountId(id: String) extends DomainError

    final case class InsufficientFunds(requested: BigDecimal) extends DomainError

    final case class AccountAlreadyExists(id: String) extends DomainError

    final case class InvalidAmount(value: BigDecimal) extends DomainError

    /** Wraps unexpected infrastructure errors. */
    final case class TechnicalFailure(msg: String) extends DomainError
    
    final case class ConcurrentModification(id: String) extends DomainError
  }
}
