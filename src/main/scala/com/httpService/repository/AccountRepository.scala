package com.httpService.repository

import cats.effect.IO
import com.httpService.domain.Models.Account
import com.httpService.domain.Models.AccountId.AccountId
import doobie.ConnectionIO

trait AccountRepository {
  def createC(account: Account): ConnectionIO[Unit]
//  def create(account: Account): IO[Unit]

  def findC(id: AccountId): ConnectionIO[Option[Account]]
//  def find(id: AccountId): IO[Option[Account]]

  def updateC(account: Account): ConnectionIO[Unit]
//  def update(account: Account): IO[Unit]

  def inTransaction[A](fa: ConnectionIO[A]): IO[A]
}