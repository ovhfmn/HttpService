package com.httpService.repository

import cats.effect.{IO, Ref}
import com.httpService.domain.Models.Account
import com.httpService.domain.Models.AccountId.AccountId
import doobie.ConnectionIO


class InMemoryAccountRepository(ref: Ref[IO, Map[AccountId, Account]]) extends AccountRepository {

  def create(account: Account): IO[Unit] =
    ref.update(map => map + (account.id -> account))

  def find(id: AccountId): IO[Option[Account]] =
    ref.get.map(_.get(id))

  override def createC(account: Account): ConnectionIO[Unit] =
    throw new UnsupportedOperationException("Not supported in memory")

  def findC(id: AccountId): ConnectionIO[Option[Account]] =
    throw new UnsupportedOperationException("Not supported in memory")

  def updateC(account: Account): ConnectionIO[Unit] =
    throw new UnsupportedOperationException("Not supported in memory")

//  def update(account: Account): IO[Unit] = create(account)

  override def inTransaction[A](fa: ConnectionIO[A]): IO[A] =
    IO.raiseError(
      new UnsupportedOperationException("InMemory repo does not support transactions")
    )
}
