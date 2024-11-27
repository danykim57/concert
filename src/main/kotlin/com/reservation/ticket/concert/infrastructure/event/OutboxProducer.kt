package com.reservation.ticket.concert.infrastructure.event

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val logger: Logger = LoggerFactory.getLogger(OutboxProducer::class.java)

    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000, multiplier = 2.0))
    @Transactional
    fun publish(event: OutboxEvent): OutboxEvent {
        try {
            val topic = "${event.aggregateType}.${event.aggregateId}"
            logger.info("Publishing event to topic: $topic, eventId: ${event.eventId}")

            kafkaTemplate.send(topic, event.aggregateId.toString(), event.messagePayload).get()
            logger.info("Successfully published event: ${event.eventId}")
            return event
        } catch (e: Exception) {
            logger.error("Failed to publish event: ${event.eventId}", e)
            event.incrementRetryCount()
            logger.error("Failed to publish event: ${event.eventId}")
            return event
        }
    }
    fun publishToDeadLetterQueue(event: OutboxEvent): OutboxEvent =
        try {
            val dlqTopic = "${event.aggregateType}.${event.aggregateId}.dlq"
            kafkaTemplate.send(dlqTopic, event.aggregateId.toString(), event.messagePayload).get()
            event
        } catch (e: Exception) {
            event.incrementRetryCount()
            event
        }
}