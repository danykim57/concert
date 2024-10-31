package com.reservation.ticket.concert.interfaces.response

import com.reservation.ticket.concert.domain.dto.SeatDTO

data class SingleSeatResponse(
    val status: Int,
    val code: String,
    val seat: SeatDTO,
)