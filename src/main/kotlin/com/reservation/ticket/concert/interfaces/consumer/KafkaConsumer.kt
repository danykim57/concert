package com.reservation.ticket.concert.interfaces.consumer

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumer {

    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)

    var receivedMessage: String? = null

    @KafkaListener(topics = ["test-topic"], groupId = "test-group")
    fun listen(message: String) {
        logger.info("Received message: $message")
        receivedMessage = message
    }
}