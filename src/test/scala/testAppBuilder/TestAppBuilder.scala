package testAppBuilder

import cats.effect.kernel.Ref
import cats.effect.{IO, Resource}
import com.httpService.domain.Models.Account
import com.httpService.domain.Models.AccountId.AccountId
import com.httpService.http.AccountRoutes
import com.httpService.repository.InMemoryAccountRepository
import com.httpService.service.AccountService
import org.http4s.HttpApp

object TestAppBuilder {
  def build: Resource[IO, HttpApp[IO]] =
    for {
      ref <- Resource.eval(Ref.of[IO, Map[AccountId, Account]](Map.empty))
    } yield {
      val repo = new InMemoryAccountRepository(ref)
      val service = new AccountService(repo)
      val routes = new AccountRoutes(service).routes
      routes.orNotFound
    }
}