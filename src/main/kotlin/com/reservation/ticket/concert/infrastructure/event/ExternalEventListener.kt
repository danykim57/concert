package com.reservation.ticket.concert.infrastructure.event

import com.reservation.ticket.concert.infrastructure.external.EventApiClient
import org.springframework.stereotype.Component

@Component
class ExternalEventListener(
    private val eventApiClient: EventApiClient
): EventListener<ReservationEvent> {
}