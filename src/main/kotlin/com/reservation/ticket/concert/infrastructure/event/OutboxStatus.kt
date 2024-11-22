package com.reservation.ticket.concert.infrastructure.event

enum class OutboxStatus {
    SUCCESS,
    FAILED,
    WAITING,
}