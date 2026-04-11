package com.httpService.service

import cats.data.EitherT
import cats.effect.IO
import com.httpService.domain.domain.{Account, AccountId, AccountService, Balance, DomainError, Money}
import com.httpService.domain.domain.AccountId.AccountId
import com.httpService.repository.AccountRepository
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class LiveAccountService(private val repo: AccountRepository) {

  val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def create(id: String, balance: BigDecimal): EitherT[IO, DomainError, Account] =
    for {
      _ <- EitherT.liftF(logger.info(s"[CREATE] id=$id amount=$balance"))

      accountId <- EitherT.fromEither[IO](
        AccountId.from(id).left.map(_ => DomainError.InvalidAmount)
      )

      balance <- EitherT.fromEither[IO](
        Balance.from(balance).left.map(_ => DomainError.InvalidAmount)
      )

//      existing <- EitherT.liftF(repo.find(id))

//      _ <- EitherT.cond[IO](
//        existing.isEmpty,
//        (),
//        DomainError.AccountAlreadyExists
//      )

      account = Account(accountId, balance)

      _ <- EitherT.liftF(repo.create(account))
      _ <- EitherT.liftF(logger.info(s"[CREATE] account=$account"))
    } yield account

  def debit(id: String, amount: BigDecimal): EitherT[IO, DomainError, Account] =
    for {
      _ <- EitherT.liftF(logger.info(s"[DEBIT] id=$id amount=$amount"))

      accountId <- EitherT.fromEither[IO](
        AccountId.from(id).left.map(_ => DomainError.InvalidAmount)
      )

      account <- EitherT.fromOptionF(
        repo.find(accountId),
        DomainError.AccountNotFound
      )

      updated <- EitherT.fromEither(
        AccountService.debit(account, amount)
      )

      _ <- EitherT.liftF(repo.update(updated))
      _ <- EitherT.liftF(logger.info(s"[DEBIT] updated=$updated"))
    } yield updated

  def credit(id: String, amount: BigDecimal): EitherT[IO, DomainError, Account] =
    for {
      _ <- EitherT.liftF(logger.info(s"[CREADIT] id=$id amount=$amount"))

      accountId <- EitherT.fromEither[IO](
        AccountId.from(id).left.map(_ => DomainError.InvalidAmount)
      )

      account <- EitherT.fromOptionF(
        repo.find(accountId),
        DomainError.AccountNotFound
      )

      updated <- EitherT.fromEither(
        AccountService.credit(account, amount)
      )

      _ <- EitherT.liftF(repo.update(updated))
      _ <- EitherT.liftF(logger.info(s"[CREDIT] updated=$updated"))
    } yield updated
}
