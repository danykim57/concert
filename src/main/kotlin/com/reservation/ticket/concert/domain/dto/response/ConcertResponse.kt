package com.reservation.ticket.concert.domain.dto.response

import com.reservation.ticket.concert.domain.dto.ConcertDto

data class ConcertResponse(
    val code: String,
    val concerts: List<ConcertDto>,
)
