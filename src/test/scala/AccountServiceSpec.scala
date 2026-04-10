import cats.effect.IO
import cats.effect.kernel.Ref
import com.httpService.repository.InMemoryAccountRepository
import com.httpService.domain.domain.*
import com.httpService.domain.domain.AccountId.AccountId
import com.httpService.domain.domain.DomainError.{AccountAlreadyExists, InsufficientFunds}
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

  test("credit increases balance") {}

  test("debit reduces balance") {
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)

      repo = new InMemoryAccountRepository(ref)
      service = new LiveAccountService(repo)

      id = AccountId.from("account_2").toOption.get
      balance = Balance.from(BigDecimal(100)).toOption.get
      account = Account(id, balance)

      amount = Money.from(BigDecimal(10)).toOption.get

      result = AccountService.debit(account, amount)

    } yield {
      assertEquals(result.map(_.balance.value), Right(BigDecimal(90)))
    }
  }

  test("debit fails if insufficient funds") {
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)

      repo = new InMemoryAccountRepository(ref)
      service = new LiveAccountService(repo)

      id = AccountId.from("account_3").toOption.get
      balance = Balance.from(BigDecimal(50)).toOption.get
      account = Account(id, balance)

      amount = Money.from(BigDecimal(100)).toOption.get

      result = AccountService.debit(account, amount)

    } yield {
      assertEquals(result, Left(InsufficientFunds))
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
      assertEquals(second, Left(AccountAlreadyExists))
    }
  }
}