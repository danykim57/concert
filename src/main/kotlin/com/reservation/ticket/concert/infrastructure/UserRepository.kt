package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByIsQueuedTrue(): List<User> // 대기열에 있는 유저들을 찾는 쿼리 메서드
    fun findByUsernameAndPassword(username: String, password: String): User?
}