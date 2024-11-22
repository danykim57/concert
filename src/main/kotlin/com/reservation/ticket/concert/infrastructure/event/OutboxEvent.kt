package com.reservation.ticket.concert.infrastructure.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.reservation.ticket.concert.domain.Reservation
import java.time.LocalDateTime
import java.util.UUID

data class OutboxEvent(
    val eventId: UUID,
    val aggregateType: OutboxType,
    val aggregateId: Long,
    val messagePayload: String,
    val createdAt: LocalDateTime,
    var status: OutboxStatus,
    var retryCount: Int = 0,
) {

    fun incrementRetryCount() {
        retryCount++
    }

    constructor(reservation: Reservation, objectMapper: ObjectMapper) : this(
        eventId = UUID.randomUUID(),
        aggregateType = OutboxType.RESERVATION,
        aggregateId = reservation.id,
        messagePayload = objectMapper.writeValueAsString(reservation),
        createdAt = LocalDateTime.now(),
        status = OutboxStatus.WAITING,
        retryCount = 0,
    )
}
