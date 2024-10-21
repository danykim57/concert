package com.reservation.ticket.concert.domain

import com.reservation.ticket.concert.domain.dto.SeatDTO
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Seat(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column
    val seatNumber: String,

    @Column
    var isAvailable: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "concert_id")
    val concert: Concert,

    @Column
    var price: Double,

    @Version
    var version: Long? = null,

    @CreatedDate  // 엔티티가 생성될 때 자동으로 설정됨
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate  // 엔티티가 수정될 때 자동으로 설정됨
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null

)

fun Seat.toDto(): SeatDTO {
    return SeatDTO(
        id = this.id,
        seatNumber = this.seatNumber,
        isAvailable = this.isAvailable
    )
}

