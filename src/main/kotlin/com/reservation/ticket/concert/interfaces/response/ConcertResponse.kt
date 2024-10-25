package com.reservation.ticket.concert.interfaces.response

import com.reservation.ticket.concert.domain.dto.ConcertDTO

data class ConcertResponse(
    val status: Int,
    val code: String,
    val concerts: List<ConcertDTO>,
)
