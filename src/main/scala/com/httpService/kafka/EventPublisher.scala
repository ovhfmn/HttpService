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
import io.circe.syntax.*

class EventPublisher private (
    producer: KafkaProducer[IO, String, String],
    topic: String
) {

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
  def resource(broker: String, topic: String): Resource[IO, EventPublisher] =
    KafkaProducer
      .resource(
        ProducerSettings[IO, String, String]
          .withBootstrapServers(broker)
      )
      .map(producer => new EventPublisher(producer, topic))
