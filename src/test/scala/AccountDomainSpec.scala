import com.httpService.domain.domain.{Account, AccountId, AccountService, Balance, Money}
import munit.CatsEffectSuite

class AccountDomainSpec extends CatsEffectSuite {

  test("money must be positive") {
    val result = Money.from(BigDecimal(0))
    assert(result.isLeft)
  }

  test("balance cannot be negatiev") {
    val result = Balance.from(BigDecimal(-1))
    assert(result.isLeft)
  }

  test("debit full balance result in zero") {
    val account = Account(
      AccountId.from("acc1").toOption.get,
      Balance.from(BigDecimal(50)).toOption.get
    )

    val amount = Money.from(BigDecimal(50)).toOption.get

    val result = AccountService.debit(account, amount)

    assertEquals(
      result.map(_.balance.value),
      Right(BigDecimal(0))
    )
  }

  test("money addition works") {}
  test("money subtraction works") {}
  test("balance subtraction fails when negative") {}
}
