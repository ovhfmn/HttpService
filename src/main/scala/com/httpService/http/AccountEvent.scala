package com.httpService.http

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

import java.time.Instant
import java.util.UUID

enum AccountEvent:
  case AccountCreated(
                       accountId: String,
                       eventId: UUID,
                       occurredAt: Instant,
                       initialBalance: BigDecimal
                     )
  case AccountDebited(
                     accountId: String,
                     eventId: UUID,
                     occurredAt: Instant,
                     amount: BigDecimal,
                     newBalance: BigDecimal
                   )
  case AccountCredited(
                      accountId: String,
                      eventId: UUID,
                      occurredAt: Instant,
                      amount: BigDecimal,
                      newBalance: BigDecimal
                    )

object AccountEvent:
  private val createdEncoder: Encoder[AccountEvent.AccountCreated]    = deriveEncoder
  private val debitedEncoder: Encoder[AccountEvent.AccountDebited]    = deriveEncoder
  private val creditedEncoder: Encoder[AccountEvent.AccountCredited]  = deriveEncoder

  implicit val encoder: Encoder[AccountEvent] = Encoder.instance {
    case e: AccountEvent.AccountCreated =>
      createdEncoder(e).deepMerge(Json.obj("eventType" -> Json.fromString("AccountCreated")))
    case e: AccountEvent.AccountDebited =>
      debitedEncoder(e).deepMerge(Json.obj("eventType" -> Json.fromString("AccountDebited")))
    case e: AccountEvent.AccountCredited =>
      creditedEncoder(e).deepMerge(Json.obj("eventType" -> Json.fromString("AccountCredited")))
  }

  private val createdDecoder: Decoder[AccountEvent.AccountCreated]    = deriveDecoder
  private val debitedDecoder: Decoder[AccountEvent.AccountDebited]    = deriveDecoder
  private val creditedDecoder: Decoder[AccountEvent.AccountCredited]  = deriveDecoder

  implicit val decoder: Decoder[AccountEvent] = Decoder.instance { cursor =>
    cursor.get[String]("eventType").flatMap{
      case "AccountCreated"   => createdDecoder.tryDecode(cursor)
      case "AccountDebited"   => debitedDecoder.tryDecode(cursor)
      case "AccountCredited"  => creditedDecoder.tryDecode(cursor)
      case other              => Left(io.circe.DecodingFailure(s"Unknown eventType: $other", cursor.history))
    }
  }