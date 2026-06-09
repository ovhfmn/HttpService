package com.httpService.http

import cats.effect.IO
import com.httpService.domain.Models.DomainError.*
import com.httpService.domain.Models.{Account, DomainError}
import io.circe.generic.auto.deriveEncoder
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io.*
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

/**
 * Single place that owns DomainError → HTTP status mapping.
 * All errors are currently logged at INFO regardless of severity.
 */
object HttpErrorMapper {
  
  val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
  
  def toResponse(error: DomainError): IO[Response[IO]] =
    logger.info(s"[ERROR] $error") *> (
      error match {
        case AccountNotFound(_) => NotFound(ErrorResponse(
          error = "AccountNotFound", message = "Account does not exist"))
        case InsufficientFunds(_) => BadRequest(ErrorResponse(
          error = "InsufficientFunds", message = "Not enough balance"))
        case InvalidAmount(_) => BadRequest(ErrorResponse(
          error = "InvalidAmount", message = "Amount must be positive"))
        case AccountAlreadyExists(_) => Conflict(ErrorResponse(
          error = "AccountAlreadyExists", message = "Account already exists"))
        case InvalidAccountId(id) => BadRequest(ErrorResponse(
          error = "InvalidAccountId", message = s"Account id '$id' is invalid"))
        case TechnicalFailure(e) => InternalServerError(ErrorResponse(
          error = "TechnicalFailure", message = e))
        case ConcurrentModification(id) => Conflict(ErrorResponse(
          error = "ConcurrentModification", message = s"Account $id was modified concurrently"))
      }
    )
    
  def handleResult(result: Either[DomainError, Account]): IO[Response[IO]] =
    result match {
      case Left(err) => HttpErrorMapper.toResponse(err)
      case Right(acc) => Ok(AccountResponse.from(acc))
    }
}