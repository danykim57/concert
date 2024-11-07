package com.reservation.ticket.concert.infrastructure

import org.springframework.stereotype.Repository

@Repository
interface QueueRedisRepository {

    fun addToWaitQueue(userId: String, timestamp: Double): Boolean

    fun getUserRank(userId: String): Long?

    fun getUsersInRange(start: Long, end: Long): Set<String>

    fun removeFromWaitQueue(userId: String): Long

    fun setUserActive(userId: String, ttl: Long): Boolean

    fun getActiveUsers(): Set<String>

    fun completeUser(userId: String): Boolean
}