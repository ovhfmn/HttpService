package com.httpService.repository

import cats.effect.{IO, Ref}
import com.httpService.domain.Models.Account
import com.httpService.domain.Models.AccountId.AccountId


class InMemoryAccountRepository(ref: Ref[IO, Map[AccountId, Account]]) extends AccountRepository {

  def create(account: Account): IO[Unit] =
    ref.update(map => map + (account.id -> account))

  def find(id: AccountId): IO[Option[Account]] =
    ref.get.map(_.get(id))

  def update(account: Account): IO[Unit] = create(account)
}


trait AccountRepository {
  def create(account: Account): IO[Unit]

  def find(id: AccountId): IO[Option[Account]]

  def update(account: Account): IO[Unit]
}