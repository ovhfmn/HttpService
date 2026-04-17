package com.httpService.http

import cats.data.EitherT
import cats.effect.IO
import com.httpService.domain.domain.{Account, DomainError}
import com.httpService.service.AccountService
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

class AccountRoutes(service: AccountService) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "accounts" / id / "debit" =>
      (for {
        body <- EitherT.liftF(req.as[DebitRequest])
        result <- service.debit(id, body.amount)
      } yield result).value.flatMap(toHttp)

    case req @ POST -> Root / "accounts" =>
      (for {
        body <- EitherT.liftF(req.as[CreateAccountRequest])
        result <- service.create(body.id, body.balance)
        } yield result).value.flatMap(toHttp)
  }

  def toHttp(result: Either[DomainError, Account]): IO[Response[IO]] =
    result match {
      case Left(err) => HttpErrorMapper.toResponse(err)
      case Right(acc) => Ok(s"Balance: ${acc.balance}")
    }

  object AmountQueryParamMatcher extends QueryParamDecoderMatcher[String]("amount")
}
