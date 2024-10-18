package com.reservation.ticket.concert.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false) // 좌석 정보 (예약한 좌석)
    val seat: Seat,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false) // 콘서트 정보 (예약한 콘서트)
    val concert: Concert,

    @Column(name = "user_id", nullable = false) // 예약한 사용자 ID
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // 예약 상태 (RESERVED, CONFIRMED, CANCELLED)
    var status: ReservationStatus = ReservationStatus.RESERVED,

    @CreatedDate  // 엔티티가 생성될 때 자동으로 설정됨
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate  // 엔티티가 수정될 때 자동으로 설정됨
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
)

enum class ReservationStatus {
    RESERVED,  // 예약
    CONFIRMED, // 결제 완료
    CANCELLED  // 예약 취소
}