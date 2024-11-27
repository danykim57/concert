package com.reservation.ticket.concert.interfaces.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.reservation.ticket.concert.application.service.OutboxService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.infrastructure.event.OutboxEvent
import com.reservation.ticket.concert.infrastructure.event.OutboxProducer
import org.junit.jupiter.api.Assertions.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit4.SpringRunner
import java.time.Duration
import java.time.Instant
import java.util.UUID

@RunWith(SpringRunner::class)
@EmbeddedKafka(
    partitions = 1,
    topics = ["reservation-confirm-topic", "reservation-confirm-topic.dlq"],
    brokerProperties = ["log.dir=build/kafka-logs"]
)
class OutboxProducerConsumerIntegrationTest {

    private lateinit var producer: OutboxProducer

    private lateinit var consumer: OutboxConsumer

    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    private lateinit var outboxService: OutboxService // Mock this service if needed

    @Test
    fun `test outbox producer sends a message and consumer listens it`() {
        val concert = Concert(1L, "concert", 5)
        val seat = Seat("1", concert, 100.0,)
        val reservation = Reservation(
            1L,
            seat,
            concert,
            UUID.randomUUID(),
            ReservationStatus.RESERVED,
            )
        val objectMapper = ObjectMapper()
        val aggregateType = "reservation-confirm-topic"
        val aggregateId = "1"
        val messagePayload = "{\"status\":\"CONFIRMED\"}"
        val event = OutboxEvent(
            reservation = reservation,
            objectMapper = objectMapper

        )

        // Publish the message
        producer.publish(event)

        // Consumer test: verify the message is received and processed
        val consumerProps = KafkaTestUtils.consumerProps(
            "test-consumer-group",
            "true",
            embeddedKafkaBroker
        )
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProps)
        val kafkaConsumer = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(kafkaConsumer, "reservation-confirm-topic")

        val records = kafkaConsumer.poll(Duration.ofSeconds(5))
        assertEquals(1, records.count())

        val record: ConsumerRecord<String, String> = records.iterator().next()
        assertEquals(aggregateId, record.key())
        assertEquals(messagePayload, record.value())
    }
}
