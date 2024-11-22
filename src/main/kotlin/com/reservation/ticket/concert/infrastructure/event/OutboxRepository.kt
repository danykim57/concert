package com.reservation.ticket.concert.infrastructure.event

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OutboxRepository: JpaRepository<Outbox, Long> {
    fun findAllByStatus(status: OutboxStatus = OutboxStatus.FAILED): List<Outbox>

    fun findByEventId(eventId: UUID): Outbox?
}