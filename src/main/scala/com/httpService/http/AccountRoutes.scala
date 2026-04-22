package com.httpService.http

import cats.data.EitherT
import cats.effect.IO
import com.httpService.http.HttpErrorMapper.handleResult
import com.httpService.http.Requests.*
import com.httpService.service.AccountService
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

class AccountRoutes(service: AccountService) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("OK")

    case req @ POST -> Root / "accounts" / id / "debit" =>
      (for {
        body <- EitherT.liftF(req.as[DebitRequest])
        result <- service.debit(id, body.amount)
      } yield result).value.flatMap(handleResult)

    case req @ POST -> Root / "accounts" / id / "credit" =>
      (for {
        body <- EitherT.liftF(req.as[CreditRequest])
        result <- service.credit(id, body.amount)
      } yield result).value.flatMap(handleResult)

    case req @ POST -> Root / "accounts" =>
      (for {
        body <- EitherT.liftF(req.as[CreateAccountRequest])
        result <- service.create(body.id, body.balance)
      } yield result).value.flatMap(handleResult)
  }
}