package com.reservation.ticket.concert.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Queue(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: UUID,

    @Column
    var status: QueueStatus,

    @ManyToOne
    @JoinColumn(name = "concert_id")
    val concert: Concert,

    @Column(nullable = false)
    val token: UUID,

    @CreatedDate  // 엔티티가 생성될 때 자동으로 설정됨
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate  // 엔티티가 수정될 때 자동으로 설정됨
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)