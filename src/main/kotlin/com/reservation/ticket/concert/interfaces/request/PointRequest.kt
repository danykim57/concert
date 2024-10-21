package com.reservation.ticket.concert.interfaces.request

import java.util.*

data class PointRequest(
    val userId: UUID,
    val amount: Double,
)
