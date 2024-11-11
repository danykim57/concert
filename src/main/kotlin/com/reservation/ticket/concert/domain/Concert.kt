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
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class Concert(
    val name: String,
    val availableTickets: Int,
) : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var location: String = ""
        private set

    var date: LocalDateTime = LocalDateTime.now()
        private set

    constructor(id: Long, name: String, availableTickets: Int) : this(name, availableTickets) {
        this.id = id
    }

    constructor(name: String, availableTickets: Int, date: LocalDateTime) : this(name, availableTickets) {
        this.date = date
    }

    constructor(name: String, location: String, availableTickets: Int, date: LocalDateTime) : this(name, availableTickets) {
        this.date = date
        this.location = location
    }

    constructor(id: Long, name: String, location: String, date: LocalDateTime, availableTickets: Int) : this(name, availableTickets) {
        this.id = id
        this.location = location
        this.date = date
    }

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = LocalDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null

}