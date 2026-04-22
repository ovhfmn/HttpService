package com.httpService.app

import cats.effect.{IO, Resource}
import com.httpService.http.AccountRoutes
import com.httpService.repository.PostgresAccountRepository
import com.httpService.service.AccountService
import doobie.hikari.HikariTransactor
import org.http4s.HttpApp

object AppBuilder {

  def build: Resource[IO, HttpApp[IO]] =
    for {
      xa <- transactor
    } yield {
      val repo = new PostgresAccountRepository(xa)
      val service = new AccountService(repo)
      val routes = new AccountRoutes(service).routes
      routes.orNotFound
    }

  private def transactor: Resource[IO, HikariTransactor[IO]] = {
    for {
      connectEC <- Resource.eval(IO.executionContext)

      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost:5432/postgres",
        "postgres",
        "postgres",
        connectEC
      )
    } yield xa
  }
}
