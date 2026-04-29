package com.httpService.http

import cats.data.EitherT
import cats.effect.IO
import com.httpService.http.HttpErrorMapper.handleResult
import com.httpService.http.Request.*
import com.httpService.kafka.EventPublisher
import com.httpService.service.AccountService
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

class AccountRoutes(service: AccountService, publisher: EventPublisher) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("OK")

    case req @ POST -> Root / "accounts" / id / "debit" =>
      (for {
        body <- EitherT.liftF(req.as[DebitRequest])
        account <- service.debit(id, body.amount)

        _ <- EitherT.liftF(publisher.publish(
          AccountEvent.MoneyDebitedEvent(id, body.amount, account.balance.value)
        ))
      } yield account).value.flatMap(handleResult)

    case req @ POST -> Root / "accounts" / id / "credit" =>
      (for {
        body <- EitherT.liftF(req.as[CreditRequest])
        account <- service.credit(id, body.amount)

        _ <- EitherT.liftF(publisher.publish(
          AccountEvent.MoneyCreditedEvent(id, body.amount, account.balance.value)
        ))
      } yield account).value.flatMap(handleResult)

    case req @ POST -> Root / "accounts" =>
      (for {
        body <- EitherT.liftF(req.as[CreateAccountRequest])
        account <- service.create(body.id, body.balance)

        _ <- EitherT.liftF(publisher.publish(
          AccountEvent.AccountCreatedEvent(body.id, body.balance)
        ))
      } yield account).value.flatMap(handleResult)
  }
}