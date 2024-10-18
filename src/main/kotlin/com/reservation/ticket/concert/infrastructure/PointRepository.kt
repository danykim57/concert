package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.Point
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.*

interface PointRepository : JpaRepository<Point, Long> {
    // 특정 유저의 포인트 정보를 조회하는 메서드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByUserId(userId: UUID): Point?
}