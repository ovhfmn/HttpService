import cats.effect.IO
import domain.{Account, DomainError}
import domain.DomainError.{AccountNotFound, InsufficientFunds, InvalidAmount}
import org.http4s.*
import org.http4s.dsl.io.*

object HttpErrorMapper {
  def toResponse(error: DomainError): IO[Response[IO]] =
    error match {
      case AccountNotFound => NotFound("Account not found")
      case InsufficientFunds => BadRequest("Insufficient funds")
      case InvalidAmount => BadRequest("Invalid amount")
    }
    
  def handleResult(result: Either[DomainError, Account]): IO[Response[IO]] =
    result match {
      case Left(err) => HttpErrorMapper.toResponse(err)
      case Right(acc) => Ok(s"Balance: ${acc.balance}")
    }
}