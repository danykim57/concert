package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.User
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByIsQueuedTrue(): List<User>

    fun findByUsernameAndPassword(username: String, password: String): User?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: UUID): Optional<User>
}