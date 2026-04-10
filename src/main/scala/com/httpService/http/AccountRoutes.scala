package com.httpService.http

import cats.data.EitherT
import cats.effect.IO
import com.httpService.domain.domain.{Account, AccountId, Balance, DomainError, LiveAccountService, Money}
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

class AccountRoutes(service: LiveAccountService) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "accounts" / id / "debit" =>
      (for {
        body <- EitherT.liftF(req.as[DebitRequest])
        accountId <- EitherT.fromEither[IO](AccountId.from(id).left.map(_ => DomainError.InvalidAmount))
        money <- EitherT.fromEither[IO](Money.from(body.amount).left.map(_ => DomainError.InvalidAmount))
        result <- service.debit(accountId, money)
      } yield result).value.flatMap(toHttp)

    case req @ POST -> Root / "accounts" =>
      (for {
        body <- EitherT.liftF(req.as[CreateAccountRequest])
        accountId <- EitherT.fromEither[IO](AccountId.from(body.id).left.map(_ => DomainError.InvalidAmount))
        balance <- EitherT.fromEither[IO](Balance.from(body.initialBalance).left.map(_ => DomainError.InvalidAmount))
        result <- service.create(accountId, balance)
        } yield result).value.flatMap(toHttp)
  }

  def toHttp(result: Either[DomainError, Account]): IO[Response[IO]] =
    result match {
      case Left(err) => HttpErrorMapper.toResponse(err)
      case Right(acc) => Ok(s"Balance: ${acc.balance}")
    }

  object AmountQueryParamMatcher extends QueryParamDecoderMatcher[String]("amount")
}
