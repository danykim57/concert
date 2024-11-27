package com.reservation.ticket.concert.interfaces.consumer

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaTestConsumer {

    var message: String? = null

    @KafkaListener(topics = ["hello-world-topic"], groupId = "test")
    fun listen(message: String) {
        this.message = message
        println("Received message: $message")
    }
}