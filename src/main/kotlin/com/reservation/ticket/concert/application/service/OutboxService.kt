package com.reservation.ticket.concert.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.infrastructure.event.Outbox
import com.reservation.ticket.concert.infrastructure.event.OutboxEvent
import com.reservation.ticket.concert.infrastructure.event.OutboxProducer
import com.reservation.ticket.concert.infrastructure.event.OutboxRepository
import com.reservation.ticket.concert.infrastructure.event.OutboxStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OutboxService(
    private val outboxRepository: OutboxRepository,
    private val outboxProducer: OutboxProducer,
    private val objectMapper: ObjectMapper,
) {
    fun save(reservation: Reservation) {
        val event = OutboxEvent(reservation, objectMapper)
        val outbox = Outbox(event)
        outboxRepository.save(outbox)
    }

    fun save(outbox: Outbox) {
        outboxRepository.save(outbox)
    }

    fun findAllUnprocessedEvents(): List<Outbox> = outboxRepository.findAllByStatus()

    fun findByEventId(eventId: UUID): Outbox = outboxRepository.findByEventId(eventId)!!

    fun republishAllAndUpdateEvents(events: List<OutboxEvent>): List<OutboxEvent> =
        events.map { event ->
            if (event.retryCount < 3) {
                outboxProducer.publishToDeadLetterQueue(event)
            } else {
                event.status = OutboxStatus.FAILED
                event
            }
        }

    fun publishUnprocessedEvents() {
        val events = this.findAllUnprocessedEvents()
        events.map { outboxProducer.publish( it.toEvent()) }
    }

    fun markOutbox(key: String) {
        val event = this.findByEventId(UUID.fromString(key))
        val markedEvent = event.markProcessed()
        outboxRepository.save(markedEvent)
    }


}