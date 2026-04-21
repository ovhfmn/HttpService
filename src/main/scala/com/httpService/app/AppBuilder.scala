package com.httpService.app

import cats.effect.{IO, Resource}
import com.httpService.http.AccountRoutes
import com.httpService.repository.postgres.PostgresAccountRepository
import com.httpService.service.AccountService
import doobie.hikari.HikariTransactor
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.io.*
import cats.syntax.semigroupk.*

object AppBuilder {

  val healthRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("OK")
  }

  def build: Resource[IO, HttpApp[IO]] =
    for {
      xa <- transactor
    } yield {
      val repo = new PostgresAccountRepository(xa)
      val service = new AccountService(repo)
      val routes = new AccountRoutes(service).routes <+> healthRoutes
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
