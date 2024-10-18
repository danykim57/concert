package com.reservation.ticket.concert.interfaces.request

data class TokenRequest(
    val id: String,
    val password: String,
    val concertCode: String,
)
