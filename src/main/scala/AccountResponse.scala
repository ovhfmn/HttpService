import domain.{Account, Balance}

final case class AccountResponse(
                                  id: String,
                                  balance: BigDecimal
                                )

object AccountResponse {

  def from(account: Account): AccountResponse =
    AccountResponse(
      id = account.id.value,
      balance = account.balance.value
    )
}