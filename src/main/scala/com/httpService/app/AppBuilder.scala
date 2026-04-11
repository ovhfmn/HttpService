package com.httpService.app

import cats.effect.IO
import cats.effect.kernel.Ref
import com.httpService.domain.domain.{Account}
import com.httpService.domain.domain.AccountId.AccountId
import com.httpService.http.AccountRoutes
import com.httpService.repository.InMemoryAccountRepository
import com.httpService.service.LiveAccountService
import org.http4s.HttpApp

object AppBuilder {

  def build: IO[HttpApp[IO]] =
    for {
      ref <- Ref.of[IO, Map[AccountId, Account]](Map.empty)
      repo = new InMemoryAccountRepository(ref)
      service = new LiveAccountService(repo)
      routes = new AccountRoutes(service).routes
      httpApp = routes.orNotFound
    } yield httpApp
}
