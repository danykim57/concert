package com.reservation.ticket.concert.domain

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime
import java.util.UUID

@RedisHash("queue", timeToLive = 3600) //1시간
data class RedisQueue(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    val userId: UUID,

    var status: QueueStatus,

    val concert:Concert,
    @Indexed
    var token: UUID,

    @CreatedDate  // 엔티티가 생성될 때 자동으로 설정됨
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate  // 엔티티가 수정될 때 자동으로 설정됨
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)