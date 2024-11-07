package com.reservation.ticket.concert.domain

import com.reservation.ticket.concert.domain.dto.SeatDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Seat(
    val seatNumber: String,
    @ManyToOne
    @JoinColumn(name = "concert_id")
    val concert: Concert,
    val price: Double,
) : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column
    var isAvailable: Boolean = true

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = LocalDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null

    constructor(seatNumber: String, concert: Concert, price: Double, isAvailable: Boolean): this(seatNumber, concert, price) {
        this.isAvailable = isAvailable
    }

    constructor(id: Long, seatNumber: String, concert: Concert, price: Double, isAvailable: Boolean): this(seatNumber, concert, price) {
        this.isAvailable = isAvailable
        this.id = id
    }

}

fun Seat.toDto(): SeatDTO {
    return SeatDTO(
        id = this.id,
        seatNumber = this.seatNumber,
        isAvailable = this.isAvailable
    )
}

