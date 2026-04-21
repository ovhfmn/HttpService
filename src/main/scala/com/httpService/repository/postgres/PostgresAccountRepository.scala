package com.httpService.repository.postgres

import cats.effect.IO
import com.httpService.domain.Models.AccountId.AccountId
import com.httpService.domain.Models.{Account, AccountId, Balance}
import com.httpService.repository.AccountRepository
import doobie.*
import doobie.implicits.*

class PostgresAccountRepository(xa: Transactor[IO]) extends AccountRepository {
    override def create(account: Account): IO[Unit] = {
      sql"""
           INSERT INTO accounts (id, balance)
           VALUES (${account.id.value}, ${account.balance.value})
         """.update.run.transact(xa).void
    }

    override def find(id: AccountId): IO[Option[Account]] = {
      sql"""
           SELECT id, balance
           FROM accounts
           WHERE id = ${id.value}
         """
        .query[(String,BigDecimal)]
        .option
        .map(_.flatMap{
          case (id, balance) =>
            for {
              accId <- AccountId.from(id).toOption
              balance <- Balance.from(balance).toOption
            } yield Account(accId, balance)
        }).transact(xa)
    }

    override def update(account: Account): IO[Unit] = {
      sql"""
           UPDATE accounts
           SET balance = ${account.balance.value}
           WHERE id = ${account.id.value}
         """.update.run.transact(xa).void
    }
}