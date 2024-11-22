package com.reservation.ticket.concert.infrastructure.event

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.domain.Seat
import java.util.UUID

data class ReservationConfirmMessage(
    val id: Long,
    val seat: Seat,
    val concert: Concert,
    val userId: UUID,
    val status: ReservationStatus,
) {
    fun toReservation(): Reservation {
        return Reservation(
            id = this.id,
            seat = this.seat,
            concert = this.concert,
            userId = this.userId,
            status = this.status,
        )
    }
}
