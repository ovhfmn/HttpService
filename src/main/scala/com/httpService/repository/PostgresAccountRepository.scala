package com.httpService.repository

import cats.effect.IO
import cats.syntax.all.*
import com.httpService.domain.Models.AccountId.AccountId
import com.httpService.domain.Models.DomainError.ConcurrentModification
import com.httpService.domain.Models.{Account, AccountId, Balance}
import com.httpService.repository.AccountRepository
import doobie.*
import doobie.implicits.*

class PostgresAccountRepository(xa: Transactor[IO]) extends AccountRepository {

    override def createC(account: Account): ConnectionIO[Unit] =
      sql"""
             INSERT INTO accounts (id, balance)
             VALUES (${account.id.value}, ${account.balance.value})
           """.update.run.void

    override def findC(id: AccountId): ConnectionIO[Option[Account]] = {
      sql"""
         SELECT id, balance, version
         FROM accounts
         WHERE id = ${id.value}
       """
        .query[(String,BigDecimal,Long)]
        .option
        .map(_.flatMap{
          case (id, balance, v) =>
            for {
              accId <- AccountId.from(id).toOption
              balance <- Balance.from(balance).toOption
            } yield Account(accId, balance, v)
        })
    }

    def updateC(account: Account): ConnectionIO[Unit] =
      sql"""
         UPDATE accounts
         SET balance = ${account.balance.value},
             version = version + 1
         WHERE id = ${account.id.value}
            AND version = ${account.version}
       """.update.run.flatMap {
        case 1 => ().pure[ConnectionIO]
        case 0 => DomainException(ConcurrentModification(account.id.value))
          .raiseError[ConnectionIO, Unit]
        case n => new RuntimeException(s"Unexpected rows updated: $n")
          .raiseError[ConnectionIO, Unit]
      }
  
    def inTransaction[A](fa: ConnectionIO[A]): IO[A] =
      fa.transact(xa)
}