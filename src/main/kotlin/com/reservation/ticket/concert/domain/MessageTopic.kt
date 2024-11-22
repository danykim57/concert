package com.reservation.ticket.concert.domain

class MessageTopic {
    companion object {
        const val CONCERT_RESERVATION_TOPIC = "queue.concert.reservation"
        const val AFTER_PAYMENT_TOPIC = "queue.after.payment"
    }
}