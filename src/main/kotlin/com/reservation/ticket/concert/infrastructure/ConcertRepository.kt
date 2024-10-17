package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.Concert
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ConcertRepository : JpaRepository<Concert, Long> {

    // 예약 가능한 콘서트만 조회하는 쿼리
    @Query("SELECT c FROM Concert c WHERE c.availableTickets > 0 AND c.date > CURRENT_TIMESTAMP")
    fun findAvailableConcerts(): List<Concert>

    fun findByName(name: String): Concert?

    override fun findById(id: Long): Optional<Concert>
}