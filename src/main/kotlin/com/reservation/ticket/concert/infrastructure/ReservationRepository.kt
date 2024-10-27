package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID


@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {

    fun findAllByUserId(userId: UUID): List<Reservation>

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RESERVED' AND r.createdAt < :expiryTime")
    fun findUnpaidReservations(@Param("expiryTime") expiryTime: LocalDateTime): List<Reservation>
}
