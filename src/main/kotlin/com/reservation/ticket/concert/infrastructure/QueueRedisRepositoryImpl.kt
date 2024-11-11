package com.reservation.ticket.concert.infrastructure

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class QueueRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>
) : QueueRedisRepository {

    private val waitQueueKey = "waitQueue"
    private val activeUsersKey = "activeUsers"

    override fun addToWaitQueue(userId: String, timestamp: Double): Boolean {
        val zSetOps = redisTemplate.opsForZSet()
        return zSetOps.add(waitQueueKey, userId, timestamp) ?: false
    }

    override fun getUserRank(userId: String): Long? {
        val zSetOps = redisTemplate.opsForZSet()
        return zSetOps.rank(waitQueueKey, userId)
    }

    override fun getUsersInRange(start: Long, end: Long): Set<String> {
        val zSetOps = redisTemplate.opsForZSet()
        return zSetOps.range(waitQueueKey, start, end) ?: emptySet()
    }

    override fun removeFromWaitQueue(userId: String): Long {
        val zSetOps = redisTemplate.opsForZSet()
        return zSetOps.remove(waitQueueKey, userId) ?: 0
    }

    override fun setUserActive(userId: String, ttl: Long): Boolean {
        val ops = redisTemplate.opsForSet()
        ops.add(activeUsersKey, userId)
        redisTemplate.expire(activeUsersKey, ttl, TimeUnit.SECONDS)
        return true
    }

    override fun getActiveUsers(): Set<String> {
        val ops = redisTemplate.opsForSet()
        return ops.members(activeUsersKey) ?: emptySet()
    }

    override fun completeUser(userId: String): Boolean {
        val ops = redisTemplate.opsForSet()
        ops.remove(activeUsersKey, userId)
        removeFromWaitQueue(userId)
        return true
    }
}