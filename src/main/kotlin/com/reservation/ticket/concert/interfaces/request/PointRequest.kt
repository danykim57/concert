package com.reservation.ticket.concert.interfaces.request

import java.util.UUID


data class PointRequest(
    val userId: UUID,
    val amount: Double,
)
