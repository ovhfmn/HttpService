import com.httpService.domain.AccountDomainService
import com.httpService.domain.Models.{Account, AccountId, Balance, Money}
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

  test("money addition works") {
    val Right(m1) = Money.from(BigDecimal(40))
    val Right(m2) = Money.from(BigDecimal(10))

    val result = m1.add(m2)

    assertEquals(result.value, BigDecimal(50))
  }

  test("money subtraction works") {
    val Right(m1) = Money.from(BigDecimal(40))
    val Right(m2) = Money.from(BigDecimal(10))

    val result = m1.subtract(m2)

    assertEquals(result.value, BigDecimal(30))
  }

  test("debit full balance results in zero") {
    val Right(id) = AccountId.from("acc1")
    val Right(balance) = Balance.from(BigDecimal(50))
    val account = Account(id, balance)

    val Right(amount) = Money.from(BigDecimal(50))

    val result = AccountDomainService.debit(account, amount)

    assertEquals(result.map(_.balance.value), Right(BigDecimal(0)))
  }

  test("balance subtraction fails when negative") {
    val Right(balance) = Balance.from(BigDecimal(20))
    val Right(amount) = Money.from(BigDecimal(50))

    val result = balance.subtract(amount)

    assert(result.isLeft)
  }
}