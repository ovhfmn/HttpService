package com.httpService.root

import cats.effect.kernel.Ref.ApplyBuilders
import cats.effect.{IO, IOApp, Ref, Resource}
import cats.syntax.semigroupk.*
import com.comcast.ip4s.{host, port}
import com.httpService.app.AppBuilder
import com.httpService.domain.domain.{Account}
import com.httpService.domain.domain.AccountId.AccountId
import com.httpService.http.AccountRoutes
import com.httpService.repository.InMemoryAccountRepository
import com.httpService.service.LiveAccountService
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

  override def run: IO[Unit] = program

  val program: IO[Unit] =
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)
      repo = new InMemoryAccountRepository(ref)
      service = new LiveAccountService(repo)
      routes = new AccountRoutes(service).routes
        <+> healthRoutes
      httpApp = Logger.httpApp(
        logHeaders = true,
        logBody = true)(Router("/" -> routes).orNotFound)
//      httpApp <- AppBuilder.build
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8010")
        .withHttpApp(httpApp)
        .build
        .use(_ => IO.never)
    } yield ()

}