package com.reservation.ticket.concert.domain.dto.response

import com.reservation.ticket.concert.domain.dto.ConcertDto

data class ReservationResponse(
    val code: String,
    val concert: ConcertDto,
)
