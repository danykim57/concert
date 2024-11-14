package com.reservation.ticket.concert.infrastructure.event

interface EventListener<T> {
    fun handle(event:T)
}