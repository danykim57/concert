package com.reservation.ticket.concert.domain.dto.response

data class SeatResponse(
    val code: String,
    val seats: List<String>,
)
