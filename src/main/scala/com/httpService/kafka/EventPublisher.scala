package com.httpService.kafka

import cats.effect.IO
import cats.effect.kernel.Resource
import com.httpService.http.AccountEvent
import fs2.kafka.{
  KafkaProducer,
  ProducerRecord,
  ProducerRecords,
  ProducerSettings
}
import io.circe.syntax.EncoderOps

/**
 * Messages are keyed by `accountId` — required for per-account ordering in Kafka.
 * Acquire via [[EventPublisher.resource]]; do not construct directly.
 */
class EventPublisher private (
    producer: KafkaProducer[IO, String, String],
    topic: String
) {

  /**
   * Completes once the broker acknowledges the record.
   * Errors propagate as failed [[IO]].
   */
  def publish(event: AccountEvent): IO[Unit] =
    val key = event match {
      case e: AccountEvent.AccountCreated   => e.accountId
      case e: AccountEvent.AccountDebited   => e.accountId
      case e: AccountEvent.AccountCredited  => e.accountId
    }

    val record = ProducerRecord(topic, key, event.asJson.noSpaces)
    producer.produce(ProducerRecords.one(record)).flatten.void
}

object EventPublisher:
  /**
   * @param broker Bootstrap server address (e.g. `"localhost:9092"`)
   * @param topic  Target Kafka topic
   */
  def resource(broker: String, topic: String): Resource[IO, EventPublisher] =
    KafkaProducer
      .resource(
        ProducerSettings[IO, String, String]
          .withBootstrapServers(broker)
          .withProperty("enable.idempotence", "true")
          .withProperty("acks", "all")
          .withRetries(10)
          .withProperty("delivery.timeout.ms", "120000")
          .withProperty("request.timeout.ms", "30000")
      )
      .map(producer => new EventPublisher(producer, topic))
