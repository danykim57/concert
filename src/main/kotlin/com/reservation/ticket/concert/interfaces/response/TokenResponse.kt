package com.reservation.ticket.concert.interfaces.response

data class TokenResponse(
    val status: Int,
    val code: String,
    val token: String,
)
