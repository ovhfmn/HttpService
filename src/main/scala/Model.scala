import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.data.EitherT
import cats.effect.IO
import domain.AccountId.AccountId
import org.typelevel.log4cats.SelfAwareStructuredLogger

object domain {

  val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  
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

    def create(id: AccountId, balance: Balance): EitherT[IO, DomainError, Account] =
      for {
        _ <- EitherT.liftF(logger.info(s"[CREATE] id=$id amount=$balance"))

        existing <- EitherT.liftF(repo.find(id))

        _ <- EitherT.cond[IO](
          existing.isEmpty,
          (),
          DomainError.AccountAlreadyExists
        )

        account = Account(id, balance)

        _ <- EitherT.liftF(repo.create(account))
        _ <- EitherT.liftF(logger.info(s"[CREATE] account=$account"))
      } yield account

    def debit(id: AccountId, amount: Money): EitherT[IO, DomainError, Account] =
      for {
        _ <- EitherT.liftF(logger.info(s"[DEBIT] id=$id amount=$amount"))

        account <- EitherT.fromOptionF(
          repo.find(id),
          DomainError.AccountNotFound
        )

        updated <- EitherT.fromEither(
          AccountService.debit(account, amount)
        )

        _ <- EitherT.liftF(repo.update(updated))
        _ <- EitherT.liftF(logger.info(s"[DEBIT] updated=$updated"))
      } yield updated

    def credit(id: AccountId, amount: Money): EitherT[IO, DomainError, Account] =
      for {
        _ <- EitherT.liftF(logger.info(s"[CREADIT] id=$id amount=$amount"))

        account <- EitherT.fromOptionF(
          repo.find(id),
          DomainError.AccountNotFound
        )

        updated <- EitherT.fromEither(
          AccountService.credit(account, amount)
        )

        _ <- EitherT.liftF(repo.update(updated))
        _ <- EitherT.liftF(logger.info(s"[CREDIT] updated=$updated"))
      } yield updated
  }

  sealed trait DomainError
  object DomainError {
    case object AccountNotFound extends DomainError

    case object InsufficientFunds extends DomainError

    case object InvalidAmount extends DomainError

    case object AccountAlreadyExists extends DomainError
  }
}
