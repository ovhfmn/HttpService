import cats.effect.IO
import domain.{Account, DomainError}
import domain.DomainError.{AccountAlreadyExists, AccountNotFound, InsufficientFunds, InvalidAmount}
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.*
import io.circe.generic.auto.*

object HttpErrorMapper {
  def toResponse(error: DomainError): IO[Response[IO]] =
    IO.println(s"[ERROR] $error") *> (
      error match {
        case AccountNotFound => NotFound(ErrorResponse(
          error = "AccountNotFound", message = "Account does not exist"))
        case InsufficientFunds => BadRequest(ErrorResponse(
          error = "InsufficientFunds", message = "Not enough balance"))
        case InvalidAmount => BadRequest(ErrorResponse(
          error = "InvalidAmount", message = "Amount must be positive"))
        case AccountAlreadyExists => Conflict(ErrorResponse(
          error = "AccountAlreadyExists", message = "Account already exists"))
      }
    )
    
  def handleResult(result: Either[DomainError, Account]): IO[Response[IO]] =
    result match {
      case Left(err) => HttpErrorMapper.toResponse(err)
      case Right(acc) => Ok(s"Balance: ${acc.balance}")
    }
}