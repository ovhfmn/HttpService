package com.httpService.http

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, DecodingFailure, Encoder, Json}

import java.time.Instant
import java.util.UUID

/**
 * Serialised as a flat JSON envelope with an `"eventType"` discriminator field.
 * Keyed by `accountId` in Kafka to preserve per-account ordering.
 */
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

  // Subtype encoders and decoders — derived automatically via circe-generic
  private val createdEncoder: Encoder[AccountEvent.AccountCreated]    = deriveEncoder
  private val debitedEncoder: Encoder[AccountEvent.AccountDebited]    = deriveEncoder
  private val creditedEncoder: Encoder[AccountEvent.AccountCredited]  = deriveEncoder

  private val createdDecoder: Decoder[AccountEvent.AccountCreated] = deriveDecoder
  private val debitedDecoder: Decoder[AccountEvent.AccountDebited] = deriveDecoder
  private val creditedDecoder: Decoder[AccountEvent.AccountCredited] = deriveDecoder

  /** Encodes any AccountEvent as flat JSON with an explicit `eventType` discriminator field.
   *
   * Example output:
   * {{{
   * {
   *   "eventType": "AccountDebited",
   *   "accountId": "acc-1",
   *   "eventId": "...",
   *   "occurredAt": "...",
   *   "amount": 50,
   *   "newBalance": 150
   * }
   * }}}
   */
  implicit val encoder: Encoder[AccountEvent] = Encoder.instance {
    case e: AccountEvent.AccountCreated =>
      createdEncoder(e).deepMerge(Json.obj("eventType" -> Json.fromString("AccountCreated")))
    case e: AccountEvent.AccountDebited =>
      debitedEncoder(e).deepMerge(Json.obj("eventType" -> Json.fromString("AccountDebited")))
    case e: AccountEvent.AccountCredited =>
      creditedEncoder(e).deepMerge(Json.obj("eventType" -> Json.fromString("AccountCredited")))
  }

  /** Decodes a flat JSON payload into the correct AccountEvent subtype using the `eventType` field.
   * Unknown event types are rejected with a DecodingFailure — malformed events are routed to DLQ.
   */
  implicit val decoder: Decoder[AccountEvent] = Decoder.instance { cursor =>
    cursor.get[String]("eventType").flatMap{
      case "AccountCreated"   => createdDecoder.tryDecode(cursor)
      case "AccountDebited"   => debitedDecoder.tryDecode(cursor)
      case "AccountCredited"  => creditedDecoder.tryDecode(cursor)
      case other              => Left(DecodingFailure(s"Unknown eventType: $other", cursor.history))
    }
  }