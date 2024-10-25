package com.reservation.ticket.concert.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID


@Entity
@EntityListeners(AuditingEntityListener::class)
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val reservationId: Long,

    @Column(nullable = false)
    val amount: Double,

    val type: PaymentType,

    @CreatedDate  // 엔티티가 생성될 때 자동으로 설정됨
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate  // 엔티티가 수정될 때 자동으로 설정됨
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)