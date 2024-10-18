package com.reservation.ticket.concert.interfaces.response

import com.reservation.ticket.concert.domain.dto.SeatDTO

data class SeatResponse(
    val code: String,
    val seats: List<SeatDTO>,
)
