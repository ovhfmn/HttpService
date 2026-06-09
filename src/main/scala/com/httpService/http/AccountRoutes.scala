package com.httpService.http

import cats.data.EitherT
import cats.effect.IO
import com.httpService.http.HttpErrorMapper.handleResult
import com.httpService.http.Request.{CreateAccountRequest,CreditRequest,DebitRequest}
import com.httpService.kafka.EventPublisher
import com.httpService.service.AccountService
import io.circe.generic.auto.deriveDecoder
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.given
import org.http4s.dsl.io.*

import java.time.Instant
import java.util.UUID

/**
 * Note: the Kafka publish happens after the DB transaction commits.
 * A publish failure does not roll back the committed state.
 */
class AccountRoutes(service: AccountService, publisher: EventPublisher) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("OK")

    case req @ POST -> Root / "accounts" / id / "debit" =>
      (for {
        body <- EitherT.liftF(req.as[DebitRequest])
        account <- service.debit(id, body.amount)

        _ <- EitherT.liftF(publisher.publish(
          AccountEvent.AccountDebited(id, UUID.randomUUID(), Instant.now(), body.amount, account.balance.value)
        ))
      } yield account).value.flatMap(handleResult)

    case req @ POST -> Root / "accounts" / id / "credit" =>
      (for {
        body <- EitherT.liftF(req.as[CreditRequest])
        account <- service.credit(id, body.amount)

        _ <- EitherT.liftF(publisher.publish(
          AccountEvent.AccountCredited(id, UUID.randomUUID(), Instant.now(), body.amount, account.balance.value)
        ))
      } yield account).value.flatMap(handleResult)

    case req @ POST -> Root / "accounts" =>
      (for {
        body <- EitherT.liftF(req.as[CreateAccountRequest])
        account <- service.create(body.id, body.balance)

        _ <- EitherT.liftF(publisher.publish(
          AccountEvent.AccountCreated(body.id, UUID.randomUUID(), Instant.now(), body.balance)
        ))
      } yield account).value.flatMap(handleResult)
  }
}