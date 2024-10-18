package com.reservation.ticket.concert.interfaces.request

data class BookingRequest(
    val concertId: Long,
    val seatId: Long,
    val queueToken: String
)
