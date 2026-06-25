package com.httpService.repository

import cats.effect.IO
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.applicativeError.catsSyntaxApplicativeErrorId
import cats.syntax.functor.toFunctorOps
import com.httpService.domain.Models.DomainError.ConcurrentModification
import com.httpService.domain.Models.{Account, AccountId, Balance, OverdraftLimit}
import com.httpService.repository.AccountRepository
import doobie.implicits.{toConnectionIOOps, toSqlInterpolator}
import doobie.{ConnectionIO, Transactor}

/**
 * Doobie-backed PostgreSQL implementation of [[AccountRepository]].
 *
 * All SQL operations return [[doobie.ConnectionIO]] programs and must be
 * composed and executed via [[inTransaction]] to participate in a transaction.
 *
 * Optimistic locking is implemented in [[updateC]] via a `WHERE version = ?`
 * predicate; zero affected rows signals a concurrent modification.
 *
 * @param xa The Doobie [[Transactor]] used to run `ConnectionIO` programs against PostgreSQL.
 */

class PostgresAccountRepository(xa: Transactor[IO]) extends AccountRepository {

    override def createC(account: Account): ConnectionIO[Unit] =
      sql"""
             INSERT INTO accounts (id, balance, overdraft_limit)
             VALUES (${account.id.value}, ${account.balance.value}, ${account.overdraftLimit.value})
           """.update.run.void

    override def findC(id: AccountId): ConnectionIO[Option[Account]] = {
      sql"""
         SELECT id, balance, overdraft_limit, version
         FROM accounts
         WHERE id = ${id.value}
       """
        .query[(String, BigDecimal, BigDecimal, Long)]
        .option
        .map(_.flatMap{
          case (id, balance, overdraftLimit, v) =>
            for {
              accId <- AccountId.from(id).toOption
              bal   <- Balance.from(balance).toOption
              limit <- OverdraftLimit.from(overdraftLimit).toOption
            } yield Account(accId, bal, limit, v)
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