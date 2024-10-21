package com.reservation.ticket.concert.interfaces.response

import com.reservation.ticket.concert.domain.dto.SeatDTO

data class ReservationResponse(
    val code: String,
    val seat: SeatDTO,
)
