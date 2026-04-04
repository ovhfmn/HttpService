import cats.effect.IO
import cats.effect.kernel.Ref
import domain.*
import domain.AccountId.AccountId
import munit.CatsEffectSuite

class AccountServiceSpec extends CatsEffectSuite {

  test("created account succeeds") {
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)

      repo = new InMemoryAccountRepository(ref)
      service = new LiveAccountService(repo)

      id = AccountId.from("account_1").toOption.get
      balance = Balance.from(BigDecimal(0)).toOption.get

      account <- service.create(id, balance).value
    } yield {
      assertEquals(account.isRight, true)
    }
  }

  test("account w/ the same id should not be created twice") {
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)

      repo = new InMemoryAccountRepository(ref)
      service = new LiveAccountService(repo)

      id = AccountId.from("account_1").toOption.get
      balance = Balance.from(BigDecimal(100)).toOption.get

      first <- service.create(id, balance).value
      second <- service.create(id, balance).value
    } yield {
      assertEquals(first.isRight, true)
      assertEquals(second, Left(DomainError.AccountAlreadyExists))
    }
  }
}