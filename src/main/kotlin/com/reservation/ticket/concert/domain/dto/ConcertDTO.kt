package com.reservation.ticket.concert.domain.dto

import java.time.LocalDateTime

data class ConcertDTO(
    val id: Long,
    val name: String,
    val location: String,
    val date: LocalDateTime,
    val availableTickets: Int
)