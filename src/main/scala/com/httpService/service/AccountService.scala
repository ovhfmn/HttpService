package com.httpService.service

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.applicativeError.catsSyntaxApplicativeErrorId
import com.httpService.domain.AccountDomainService
import com.httpService.domain.Models.*
import com.httpService.repository.{AccountRepository, DomainException}
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

/**
 * Orchestrates validation, DB transactions, and error recovery.
 * [[DomainException]] is caught from the `IO` error channel and re-raised
 * into the `EitherT` error channel.
 */
class AccountService(private val repo: AccountRepository) {

  private val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def healthCheck: IO[Boolean] = 
    repo.inTransaction(sql"SELECT 1".query[Int].unique)
      .attempt.map(_.isRight)

  def create(id: String, balance: BigDecimal, overdraftLimit: BigDecimal): EitherT[IO, DomainError, Account] =
    for {
      _ <- EitherT.liftF(logger.info(Map(
        "action" -> "CREATE",
        "id" -> id,
        "amount" -> balance.toString,
        "overdraftLimit" -> overdraftLimit.toString
      ))("### Account create initiated"))

      accountId <- EitherT.fromEither[IO](AccountId.from(id)
        .left.map(_ => DomainError.InvalidAccountId(id)))

      validatedBalance <- EitherT.fromEither[IO](Balance.from(balance)
        .left.map(_ => DomainError.InvalidAmount(balance)))

      overdraftLimit <- EitherT.fromEither[IO](OverdraftLimit.from(overdraftLimit)
        .left.map(_ => DomainError.InvalidOverdraftLimit(overdraftLimit)))

      account <- runTransaction(repo.inTransaction(createTx(accountId, validatedBalance, overdraftLimit)))

      _ <- EitherT.liftF(logger.info(Map(
        "action" -> "CREATE",
        "id" -> account.id.value,
        "amount" -> account.balance.value.toString
      ))("### Account created"))
    } yield account

  private def createTx(accountId: AccountId, balance: Balance, overdraftLimit: OverdraftLimit): ConnectionIO[Account] =
    for {
      existing <- repo.findC(accountId)

      _ <- existing match {
        case Some(_) => DomainException(DomainError.AccountAlreadyExists(accountId.value))
          .raiseError[ConnectionIO, Unit]
        case None => 
          ().pure[ConnectionIO]
      }

      account = Account(accountId, balance, overdraftLimit)

      _ <- repo.createC(account)

    } yield account
    
  def debit(id: String, amount: BigDecimal): EitherT[IO, DomainError, Account] =
    for {
      _ <- EitherT.liftF(logger.info(Map(
        "action" -> "DEBIT",
        "id" -> id,
        "amount" -> amount.toString
      ))("### Debit initiated"))

      accountId <- EitherT.fromEither[IO](AccountId.from(id)
        .left.map(_ => DomainError.InvalidAccountId(id)))

      money <- EitherT.fromEither[IO](Money.from(amount)
        .left.map(_ => DomainError.InvalidAmount(amount)))

      account <- runTransaction(repo.inTransaction(debitTx(accountId, money)))

      _ <- EitherT.liftF(logger.info(Map(
        "action" -> "DEBIT",
        "id" -> account.id.value,
        "balance" -> account.balance.value.toString
      ))("### Debit completed"))
    } yield account

  def credit(id: String, amount: BigDecimal): EitherT[IO, DomainError, Account] =
    for {
      _ <- EitherT.liftF(logger.info(Map(
        "action" -> "CREDIT",
        "id" -> id,
        "amount" -> amount.toString
      ))("### Credit initiated"))

      accountId <- EitherT.fromEither[IO](AccountId.from(id)
        .left.map(_ => DomainError.InvalidAccountId(id)))

      money <- EitherT.fromEither[IO](Money.from(amount)
        .left.map(_ => DomainError.InvalidAmount(amount)))

      account <- runTransaction(repo.inTransaction(creditTx(accountId, money)))

      _ <- EitherT.liftF(logger.info(Map(
        "action" -> "CREDIT",
        "id" -> account.id.value,
        "balance" -> account.balance.value.toString
      ))("### Credit completed"))
    } yield account

  private def debitTx(accountId: AccountId, amount: Money): ConnectionIO[Account] = {
    for {
      maybeAccount <- repo.findC(accountId)

      account <- maybeAccount match {
        case Some(acc) => acc.pure[ConnectionIO]
        case None => DomainException(DomainError.AccountNotFound(accountId.value))
          .raiseError[ConnectionIO, Account]
      }

      updated <- AccountDomainService.debit(account, amount) match {
        case Left(e) => DomainException(e).raiseError[ConnectionIO, Account]
        case Right(acc) => acc.pure[ConnectionIO]
      }

      _ <- repo.updateC(updated)
    } yield updated
  }

  private def creditTx(accountId: AccountId, money: Money): ConnectionIO[Account] =
    for {
      maybeAccount <- repo.findC(accountId)

      account <- maybeAccount match {
        case Some(acc) => acc.pure[ConnectionIO]
        case None => DomainException(DomainError.AccountNotFound(accountId.value))
          .raiseError[ConnectionIO, Account]
      }

      updated <- AccountDomainService.credit(account, money) match {
        case Left(err) => DomainException(err).raiseError[ConnectionIO, Account]
        case Right(acc) => acc.pure[ConnectionIO]
      }

      _ <- repo.updateC(updated)

    } yield updated

  private def runTransaction[Account](io: IO[Account]): EitherT[IO, DomainError, Account] = {
    EitherT(
      io.attempt.map {
        case Right(account) => Right(account)
        case Left(DomainException(e)) => Left(e)
        case Left(e) => Left(DomainError.TechnicalFailure("Technical Failure"))
      }
    )
  }
}
