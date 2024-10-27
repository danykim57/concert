package com.reservation.ticket.concert.interfaces.response

import com.reservation.ticket.concert.domain.dto.SeatDTO

data class SeatResponse(
    val status: Int,
    val code: String,
    val seats: List<SeatDTO>,
)
