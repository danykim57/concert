package com.reservation.ticket.concert.interfaces.request

import com.reservation.ticket.concert.domain.dto.ConcertDTO

data class ConcertRequest(
    val code: String,
    val concert: ConcertDTO,
)
