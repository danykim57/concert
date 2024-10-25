package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.PointHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PointHistoryRepository: JpaRepository<PointHistory, Long> {

    override fun findById(id: Long): Optional<PointHistory>

    fun findAllByUserId(userId: UUID): List<PointHistory>

    fun save(entity: PointHistory): PointHistory
}