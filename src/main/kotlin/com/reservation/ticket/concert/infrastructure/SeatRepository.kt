package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.Seat
import jakarta.persistence.LockModeType
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.time.LocalDateTime
import java.util.Optional

interface SeatRepository : JpaRepository<Seat, Long> {
    // 콘서트 날짜 범위에 맞는 좌석을 조회하는 쿼리 메서드
    @Cacheable(value = ["availableSeats"], cacheManager = "redisCacheManager")
    fun findByConcertDateBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Seat>

    override fun findById(id: Long): Optional<Seat>


    fun save(seat: Seat): Seat

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWriteLockById(seatId: Long): Optional<Seat>
}