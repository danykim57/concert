package com.reservation.ticket.concert.infrastructure.event

import com.reservation.ticket.concert.domain.Outbox
import org.springframework.stereotype.Repository

@Repository
class OutboxEventRepository(
    private val outboxRepository: OutboxRepository
){

    fun save(event: OutboxEvent) {
        val dataModel = Outbox(event)
        outboxRepository.save(dataModel)
    }

    fun saveAll(updatedEvents: List<OutboxEvent>) {
        val dataModels = updatedEvents.map { Outbox(it) }
        outboxRepository.saveAll(dataModels)
    }

}