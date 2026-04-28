package com.httpService.kafka

import cats.effect.IO
import com.httpService.http.{AccountEvent, Request}
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings}
import io.circe.syntax.*

class EventPublisher(broker: String, topic: String) {
  private val  settings = ProducerSettings[IO, String, String]
    .withBootstrapServers(broker)

  def publish(event: AccountEvent): IO[Unit] =
    KafkaProducer.resource(settings).use { producer =>
      val key = event match {
        case e: AccountEvent.AccountCreatedEvent => e.id
        case e: AccountEvent.MoneyDebitedEvent => e.id
        case e: AccountEvent.MoneyCreditedEvent => e.id
      }

      val record = ProducerRecord(topic, key, event.asJson.noSpaces)
      producer.produce(ProducerRecords.one(record)).flatten.void
    }
}
