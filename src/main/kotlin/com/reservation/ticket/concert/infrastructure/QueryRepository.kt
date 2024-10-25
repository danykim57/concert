package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.domain.QueueStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QueueRepository : JpaRepository<Queue, Long> {
    // 유저가 대기열에 있는지 조회
    fun findByUserId(userId: UUID): Queue?

    fun save(queue: Queue): Queue

    fun findByUserIdAndConcert(userId: UUID, concert: Concert): Queue?

    fun findAllByConcertAndStatus(concert: Concert, status: QueueStatus): List<Queue>

    // 대기열에 있는 모든 유저를 대기 순서대로 정렬하여 조회
    fun findAllByOrderByCreatedAtAsc(): List<Queue>
}