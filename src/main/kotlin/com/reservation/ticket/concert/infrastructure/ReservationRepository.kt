package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*


@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {

    fun findBySeatId(seatId: Long): Reservation?

    fun findAllByUserId(userId: UUID): List<Reservation>

    fun findByConcertIdAndUserId(concertId: Long, userId: UUID): Reservation?

    fun findAllByConcertIdAndStatus(concertId: Long, status: ReservationStatus): List<Reservation>

    fun existsBySeatIdAndStatus(seatId: Long, status: ReservationStatus): Boolean

    fun countByConcertIdAndStatus(concertId: Long, status: ReservationStatus): Long

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RESERVED' AND r.createdAt < :expiryTime")
    fun findUnpaidReservations(@Param("expiryTime") expiryTime: LocalDateTime): List<Reservation>
}
