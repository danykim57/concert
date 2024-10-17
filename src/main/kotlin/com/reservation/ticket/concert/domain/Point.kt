package com.reservation.ticket.concert.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Point(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,                    // 포인트 엔티티의 고유 ID

    var amount: Double,                     // 포인트 금액

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    val user: User,
    @CreatedDate  // 엔티티가 생성될 때 자동으로 설정됨
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate  // 엔티티가 수정될 때 자동으로 설정됨
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)