package com.reservation.ticket.concert.infrastructure.event

interface EventPublisher<T> {
    fun publish(event: T)
}