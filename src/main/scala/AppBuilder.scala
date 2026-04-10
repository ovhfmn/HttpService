import cats.effect.IO
import cats.effect.kernel.Ref
import domain.AccountId.AccountId
import domain.{Account, LiveAccountService}
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
