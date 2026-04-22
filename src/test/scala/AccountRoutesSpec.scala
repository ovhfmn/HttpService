import cats.effect.IO
import com.httpService.http.CreateAccountRequest
import com.httpService.http.Requests.DebitRequest
import io.circe.generic.auto.*
import io.circe.syntax.*
import munit.CatsEffectSuite
import org.http4s.circe.*
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Status}
import testAppBuilder.TestAppBuilder


class AccountRoutesSpec extends CatsEffectSuite {

  test("POST /accounts create account") {
    val req = Request[IO](Method.POST, uri"/accounts")
      .withEntity(CreateAccountRequest("acc1", BigDecimal(100)).asJson)

    TestAppBuilder.build.use { app =>
      app.run(req).map { response =>
        assertEquals(response.status, Status.Ok)
      }
    }
  }

  test("POST /accounts returns error if already exists") {
    val req = Request[IO](Method.POST, uri"/accounts")
      .withEntity(CreateAccountRequest("acc1", BigDecimal(100)).asJson)

    TestAppBuilder.build.use { app =>
      app.run(req).map { response =>
        assertEquals(response.status, Status.Ok)
      }
    }
  }

  test("POST /accounts fails on invalid input") {
    val req = Request[IO](Method.POST, uri"/accounts")
      .withEntity(CreateAccountRequest("", BigDecimal(-10)).asJson)

    TestAppBuilder.build.use { app =>
      app.run(req).map { response =>
        assertEquals(response.status, Status.BadRequest)
      }
    }
  }

  test("POST /accounts/{id}/debit works") {
    val createReq = Request[IO](Method.POST, uri"/accounts")
      .withEntity(CreateAccountRequest("acc1", BigDecimal(100)).asJson)

    val debitReq = Request[IO](Method.POST, uri"/accounts/acc1/debit")
      .withEntity(DebitRequest(BigDecimal(10)).asJson)

    TestAppBuilder.build.use { app =>
      app.run(createReq).map { _ =>
        app.run(debitReq).map { response =>
          assertEquals(response.status, Status.Ok)
        }
      }
    }
  }

  test("POST /accounts/{id}/debit fails for unknown account") {
    val req = Request[IO](Method.POST, uri"/accounts/unknown/debit")
      .withEntity(DebitRequest(BigDecimal(10)).asJson)

    TestAppBuilder.build.use { app =>
      app.run(req).map { response =>
        assertEquals(response.status, Status.NotFound)
      }
    }
  }
}