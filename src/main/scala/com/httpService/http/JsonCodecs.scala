package com.httpService.http

import cats.effect.IO
import com.httpService.http.Request.{DebitRequest,CreditRequest,CreateAccountRequest}
import io.circe.generic.auto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf


/** http4s [[EntityDecoder]] instances for all inbound request body types. */
class JsonCodecs {

  given EntityDecoder[IO, DebitRequest] =
    jsonOf[IO, DebitRequest]

  given EntityDecoder[IO, CreditRequest] =
    jsonOf[IO, CreditRequest]

  given EntityDecoder[IO, CreateAccountRequest] =
    jsonOf[IO, CreateAccountRequest]

}