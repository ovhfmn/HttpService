import cats.effect.IO
import com.httpService.app.AppBuilder
import com.httpService.http.CreateAccountRequest
import io.circe.generic.auto.*
import io.circe.syntax.*
import munit.CatsEffectSuite
import org.http4s.circe.*
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Status}


class AccountRoutesSpec extends CatsEffectSuite {


  test("POST /accounts create account") {
    val req = Request[IO](Method.POST, uri"/accounts")
      .withEntity(CreateAccountRequest("acc1", BigDecimal(100)).asJson)

    for {
      app <- AppBuilder.build
      response <- app.run(req)
    } yield {
      assertEquals(response.status, Status.Ok)
    }
  }

  test("POST /debit debit account") {
  }

  test("POST /accounts fails on invalid input") {}
  test("POST /accounts returns error if already exists") {}
  test("POST /debit fails for unknown account") {}

}