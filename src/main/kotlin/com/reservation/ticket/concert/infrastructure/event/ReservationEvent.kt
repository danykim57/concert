package com.reservation.ticket.concert.infrastructure.event

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import java.util.UUID

data class ReservationEvent(
    val userId: UUID,
    val concert: Concert,
    val seatId: Long,
    val reservationId: Long,
    val reservationStatus: ReservationStatus,
) {
    companion object {
        fun from(reservation: Reservation): ReservationEvent {
            return ReservationEvent(
                userId = reservation.userId,
                concert = reservation.concert,
                seatId = reservation.seat.id,
                reservationId = reservation.id,
                reservationStatus = reservation.status,
            )
        }
    }
}
