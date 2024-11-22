package com.reservation.ticket.concert.infrastructure.event

import com.reservation.ticket.concert.application.service.OutboxService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload

class OutboxConsumer(
    private val logger: Logger = LoggerFactory.getLogger(OutboxConsumer::class.java),
    private val outboxService: OutboxService
) {

    @KafkaListener(
        topics = ["reservation-confirm-topic"],
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun listen(
        @Payload event: String,
        @Header("kafka_receivedMessageKey") key: String,
        acknowledgment: Acknowledgment,
    ) {
        try {
            logger.info("Received payment.created event with key: $key" )
            outboxService.markOutbox(key)
            acknowledgment.acknowledge()
            logger.info("Successfully processed payment.created event with key: $key" )
        } catch (e: Exception) {
            logger.error("Failed to process payment.created event with key: $key")
            throw e
        }
    }
}