package com.httpService.app

import cats.effect.{IO, Resource}
import com.httpService.http.AccountRoutes
import com.httpService.repository.PostgresAccountRepository
import com.httpService.service.AccountService
import doobie.hikari.HikariTransactor
import org.http4s.HttpApp
import com.httpService.config.AppConfig
import com.httpService.config.ConfigLoader
import com.httpService.kafka.EventPublisher
import com.httpService.middleware.CorrelationIdMiddleware

/**
 * Resources are released in reverse acquisition order on shutdown:
 * Kafka producer → transactor → (config needs no release).
 */
object AppBuilder {

  def build: Resource[IO, (HttpApp[IO], AppConfig)] =
    for {
      config    <- Resource.eval(ConfigLoader.load)
      xa        <- transactor(config)
      publisher <- EventPublisher.resource("redpanda:29092", "account-events")
    } yield {
      val repo = new PostgresAccountRepository(xa)
      val service = new AccountService(repo)
      val routes = new AccountRoutes(service, publisher).routes
      val app = CorrelationIdMiddleware(routes.orNotFound)
      (app, config)
    }

  private def transactor(config: AppConfig): Resource[IO, HikariTransactor[IO]] = {
    for {
      connectEC <- Resource.eval(IO.executionContext)
      xa <- HikariTransactor.newHikariTransactor[IO](
        config.db.driver,
        config.db.url,
        config.db.user,
        config.db.password,
        connectEC
      )
    } yield xa
  }
}
