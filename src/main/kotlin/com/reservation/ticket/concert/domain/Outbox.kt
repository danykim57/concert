package com.reservation.ticket.concert.domain

import com.reservation.ticket.concert.infrastructure.event.OutboxEvent
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.UUID

@Entity
class Outbox(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val eventId: UUID,
    @Enumerated(EnumType.STRING)
    val aggregateType: OutboxType,
    val aggregateId: Long,
    val messagePayload: String,
    val createdAt: LocalDateTime,
    @Enumerated(EnumType.STRING)
    var status: OutboxStatus,
    var retryCount: Int = 0,
) {
    constructor(event: OutboxEvent) : this(
        0L,
        event.eventId,
        event.aggregateType,
        event.aggregateId,
        event.messagePayload,
        event.createdAt,
        event.status,
        event.retryCount,
    )

    fun markProcessed(): Outbox {
        this.status = OutboxStatus.SUCCESS
        return this
    }

    fun toEvent(): OutboxEvent {
        return OutboxEvent(
            this.eventId,
            this.aggregateType,
            this.aggregateId,
            this.messagePayload,
            this.createdAt,
            this.status,
            this.retryCount,
        )
    }
}