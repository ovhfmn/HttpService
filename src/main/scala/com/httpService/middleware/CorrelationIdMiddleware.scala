package com.httpService.middleware

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.{HttpApp, Request}
import org.slf4j.MDC

import java.util.UUID
import scala.language.postfixOps

/**
 * Reads `X-Correlation-ID` from the request, or generates a UUID if absent.
 * Writes it to the SLF4J MDC for the duration of the request, then echoes it
 * in the response header.
 *
 * Caveat: MDC is ThreadLocal-backed. Under Cats Effect 3's work-stealing
 * scheduler, the MDC value may not be visible on threads handling subsequent
 * steps of the same fiber.
 */
object CorrelationIdMiddleware {

  private val CorrelationIdHeader = "X-Correlation-ID"
  private val MdcKey = "correlation_id"

  def apply(app: HttpApp[IO]): HttpApp[IO] = {
    Kleisli { (req: Request[IO]) =>
      val correlationId = req.headers
        .get(org.typelevel.ci.CIString(CorrelationIdHeader))
        .map(_.head.value)
        .getOrElse(UUID.randomUUID().toString)

      IO.delay(MDC.put(MdcKey, correlationId)) *>
        app.run(req)
          .map(_.putHeaders(
            org.http4s.Header.Raw(
              org.typelevel.ci.CIString(CorrelationIdHeader),
              correlationId)
          )).guarantee(IO.delay(MDC.remove(MdcKey)))
    }
  }
}