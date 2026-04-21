package com.httpService.root

import cats.effect.{IO, IOApp, Ref}
import cats.syntax.semigroupk.*
import com.comcast.ip4s.{host, port}
import com.httpService.app.AppBuilder
import com.httpService.domain.Models.Account
import com.httpService.domain.Models.AccountId.AccountId
import com.httpService.http.AccountRoutes
import com.httpService.repository.InMemoryAccountRepository
import com.httpService.service.AccountService
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

object Main extends IOApp.Simple {

  val healthRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("OK")
  }

  override def run: IO[Unit] = program2

  val program: IO[Unit] =
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)
      repo = new InMemoryAccountRepository(ref)
      service = new com.httpService.service.AccountService(repo)
      routes = new AccountRoutes(service).routes
        <+> healthRoutes
      httpApp = Logger.httpApp(
        logHeaders = true,
        logBody = true)(Router("/" -> routes).orNotFound)
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8010")
        .withHttpApp(httpApp)
        .build
        .use(_ => IO.never)
    } yield ()

  val program2: IO[Unit] =
    AppBuilder.build.use { httpApp =>
      EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8010")
        .withHttpApp(httpApp)
        .build
        .useForever
    }
}