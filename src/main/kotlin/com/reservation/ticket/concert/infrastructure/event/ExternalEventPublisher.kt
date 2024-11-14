package com.reservation.ticket.concert.infrastructure.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class ExternalEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
): EventPublisher<ReservationEvent>{

    override fun publish(event: ReservationEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}