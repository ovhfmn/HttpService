import cats.effect.IO
import domain.AccountId.AccountId
import domain.DomainError.InvalidAmount

object domain {

  object AccountId {
    opaque type AccountId = String

    def from(value: String): Either[String, AccountId] =
      if (value.trim.isEmpty) Left("AccountId cannot be empty")
      else Right(value)
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
  }

  final case class Account(
                            id: AccountId,
                            balance: Balance
                          )

  object AccountService {
    import domain.Balance.*

    def debit(account: Account, amount: Money): Either[DomainError, Account] =
      if (account.balance < amount)
        Left(DomainError.InsufficientFunds)
      else
      account.balance subtract amount match
        case Left(_) => Left(DomainError.InsufficientFunds)
        case Right(newBalance) =>
          Right(account.copy(balance = newBalance))

    def credit(account: Account, amount: Money): Either[DomainError, Account] =
        Right(account.copy(balance = account.balance add amount))
  }

  class LiveAccountService(repo: AccountRepository) {

    def create(id: AccountId, balance: Balance): IO[Either[DomainError, Account]] =
      repo.find(id).flatMap {
        case Some(_) => IO.pure(Left(InvalidAmount))
        case None =>
          val account = Account(id, balance)
          repo.create(account).as(Right(account))
      }

    def debit(id: AccountId, amount: Money): IO[Either[DomainError, Account]] =
      repo.find(id).flatMap {
        case None =>
          IO.pure(Left(DomainError.AccountNotFound))
        case Some(account) =>
          AccountService.debit(account, amount) match {
            case Left(err) => IO.pure(Left(err))
            case Right(updated) =>
              repo.update(updated).as(Right(updated))
          }
      }

    def credit(id: AccountId, amount: Money): IO[Either[DomainError, Account]] =
      repo.find(id).flatMap {
        case None =>
          IO.pure(Left(DomainError.AccountNotFound))

        case Some(account) =>
          AccountService.credit(account, amount) match {
            case Left(err) =>
              IO.pure(Left(err))

            case Right(updated) =>
              repo.update(updated).as(Right(updated))
          }
      }
  }

  sealed trait DomainError
  object DomainError {
    case object AccountNotFound extends DomainError

    case object InsufficientFunds extends DomainError

    case object InvalidAmount extends DomainError
  }
}
