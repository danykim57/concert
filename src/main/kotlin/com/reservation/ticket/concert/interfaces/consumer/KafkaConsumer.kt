package com.reservation.ticket.concert.interfaces.consumer

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class KafkaConsumer {

    private val logger = LoggerFactory.getLogger(KafkaConsumer::class.java)

    var receivedMessage: String? = null

    @KafkaListener(topics = ["test-topic"], groupId = "test-group")
    @Async
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun listen(message: String) {
        logger.info("Received message: $message")
        receivedMessage = message
    }
}