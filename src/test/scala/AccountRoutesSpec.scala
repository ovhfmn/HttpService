import cats.effect.IO
import cats.effect.kernel.Ref
import domain.AccountId.AccountId
import domain.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import munit.CatsEffectSuite
import org.http4s.circe.*
import org.http4s.implicits.uri
import org.http4s.{HttpApp, Method, Request, Status}


class AccountRoutesSpec extends CatsEffectSuite {

  def getApp: IO[HttpApp[IO]] =
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)
      repo = new InMemoryAccountRepository(ref)
      service = new LiveAccountService(repo)
      routes = new AccountRoutes(service).routes
    } yield routes.orNotFound

  test("POST /accounts create account") {
    val req = Request[IO](Method.POST, uri"/accounts")
      .withEntity(CreateAccountRequest("acc1", BigDecimal(100)).asJson)

    for {
      app <- getApp
      response <- app.run(req)
    } yield {
      assertEquals(response.status, Status.Ok)
    }
  }

  test("POST /debit debit account") {
  }
}