package com.reservation.ticket.concert.domain.dto.request

import com.reservation.ticket.concert.domain.dto.ConcertDto

data class ConcertRequest(
    val code: String,
    val concert: ConcertDto,
)
