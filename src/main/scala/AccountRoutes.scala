import cats.effect.IO
import domain.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.*

class AccountRoutes(service: LiveAccountService) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "accounts" / id / "debit" =>
      for {
        debitReq <- req.as[DebitRequest]
        accountId <- IO.fromEither(AccountId.from(id).left.map(new RuntimeException(_)))
        money <- IO.fromEither(Money.from(debitReq.amount).left.map(new RuntimeException(_)))
        result <- service.debit(accountId, money)
        response <- toHttp(result)
      } yield response

    case req @ POST -> Root / "accounts" =>
      for {
        body <- req.as[CreateAccountRequest]
        accountId <- IO.fromEither(AccountId.from(body.id).left.map(new RuntimeException(_)))
        balance <- IO.fromEither(Balance.from(body.initialBalance).left.map(new RuntimeException(_)))
        result <- service.create(accountId, balance)
        response <-toHttp(result)
      } yield response
  }

  def toHttp(result: Either[DomainError, Account]): IO[Response[IO]] =
    result match {
      case Left(err) => HttpErrorMapper.toResponse(err)
      case Right(acc) => Ok(s"Balance: ${acc.balance}")
    }

  object AmountQueryParamMatcher extends QueryParamDecoderMatcher[String]("amount")
}
