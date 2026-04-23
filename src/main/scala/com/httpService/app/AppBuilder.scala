package com.httpService.app

import cats.effect.{IO, Resource}
import com.httpService.http.AccountRoutes
import com.httpService.repository.PostgresAccountRepository
import com.httpService.service.AccountService
import doobie.hikari.HikariTransactor
import org.http4s.HttpApp
import com.httpService.config.AppConfig
import com.httpService.config.ConfigLoader

object AppBuilder {

  def build: Resource[IO, (HttpApp[IO], AppConfig)] =
    for {
      config <- Resource.eval(ConfigLoader.load)
      xa <- transactor(config)
    } yield {
      val repo = new PostgresAccountRepository(xa)
      val service = new AccountService(repo)
      val routes = new AccountRoutes(service).routes
      (routes.orNotFound, config)
    }

  private def transactor(config: AppConfig): Resource[IO, HikariTransactor[IO]] = {
    for {
      connectEC <- Resource.eval(IO.executionContext)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        config.db.url,
        config.db.user,
        config.db.password,
        connectEC
      )
    } yield xa
  }
}
