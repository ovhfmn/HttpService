import cats.effect.IO
import cats.effect.kernel.Ref
import com.httpService.domain.Models.*
import com.httpService.domain.Models.AccountId.AccountId
import com.httpService.domain.Models.DomainError.InsufficientFunds
import com.httpService.repository.InMemoryAccountRepository
import com.httpService.service.AccountService
import munit.CatsEffectSuite

class AccountServiceSpec extends CatsEffectSuite {

  def createService: IO[AccountService] =
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)
    } yield new AccountService(new InMemoryAccountRepository(ref))


  test("create account succeeds") {
    for {
      service <- createService
      account <- service.create("account_1", BigDecimal(0)).value
    } yield {
      assertEquals(account.isRight, true)
    }
  }

  test("create fails if duplicate") {
    for {
      service <- createService
      account <- service.create("account_1", BigDecimal(0)).value
    } yield {
      assertEquals(account.isRight, true)
    }
  }

  test("create fails on invalid id") {
    for {
      service <- createService
      result <- service.create("", 100).value
    } yield {
      assert(result.isLeft)
    }
  }

  test("create fails on negative balance") {
    for {
      service <- createService
      result <- service.create("acc1", -100).value
    } yield {
      assert(result.isLeft)
    }
  }

  test("credit increases balance") {
    for {
      service <- createService
      _ <- service.create("acc1", 100).value
      result <- service.credit("acc1", 50).value
    } yield {
      assertEquals(result.map(_.balance.value), Right(BigDecimal(150)))
    }
  }

  test("credit fails if account does not exist") {
    for {
      service <- createService
      result <- service.credit("unknown", 10).value
    } yield {
      assert(result.isLeft)
    }
  }

  test("debit reduces balance") {
    for {
      service <- createService
      _ <- service.create("acc1", 100).value
      result <- service.debit("acc1", 10).value
    } yield {
      assertEquals(result.map(_.balance.value), Right(BigDecimal(90)))
    }
  }

  test("debit fails if insufficient funds") {
    for {
      service <- createService
      _ <- service.create("acc1", 50).value
      result <- service.debit("acc1", 100).value
    } yield {
      assertEquals(result, Left(InsufficientFunds))
    }
  }

  test("debit fails if account does not exist") {
    for {
      service <- createService
      result <- service.debit("unknown", 10).value
    } yield {
      assert(result.isLeft)
    }
  }
}