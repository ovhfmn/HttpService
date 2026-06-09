package com.httpService.repository

import cats.effect.IO
import com.httpService.domain.Models.Account
import com.httpService.domain.Models.AccountId.AccountId
import doobie.ConnectionIO

/**
 * Repository algebra for [[Account]] persistence.
 *
 * `*C` methods return [[ConnectionIO]] so they can be composed into a single
 * transaction before execution. Call [[inTransaction]] to run the composed program.
 */
trait AccountRepository {
  def createC(account: Account): ConnectionIO[Unit]

  def findC(id: AccountId): ConnectionIO[Option[Account]]

  /**
   * Uses optimistic locking (`WHERE version = account.version`).
   * Zero affected rows raises [[DomainError.ConcurrentModification]] via [[DomainException]].
   */
  def updateC(account: Account): ConnectionIO[Unit]

  /** Runs `fa` in a single JDBC transaction. Rolls back on any exception. */
  def inTransaction[A](fa: ConnectionIO[A]): IO[A]
}