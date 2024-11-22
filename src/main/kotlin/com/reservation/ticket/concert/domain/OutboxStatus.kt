package com.reservation.ticket.concert.domain

enum class OutboxStatus {
    SUCCESS,
    FAILED,
    WAITING,
}