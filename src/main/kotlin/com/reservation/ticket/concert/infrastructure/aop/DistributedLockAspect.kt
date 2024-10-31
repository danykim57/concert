package com.reservation.ticket.concert.infrastructure.aop

import com.reservation.ticket.concert.infrastructure.annotation.DistributedLock
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Aspect
@Component
class DistributedLockAspect(
    private val redisTemplate: RedisTemplate<String, String>
) {
    @Around("@annotation(lockAnnotation)")
    fun around(joinPoint: ProceedingJoinPoint, lockAnnotation: DistributedLock): Any? {
        val lockKey = lockAnnotation.lockKey
        val timeout = lockAnnotation.timeout

        // Redis 분산 락 획득
        val isLocked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofMillis(timeout))
        if (isLocked == true) {
            try {
                return joinPoint.proceed()
            } finally {
                redisTemplate.delete(lockKey)
            }
        } else {
            throw IllegalStateException("Unable to acquire lock for key: $lockKey")
        }
    }
}