package com.reservation.ticket.concert.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "`user`")
class User(
    @Id
    val id: UUID = UUID.randomUUID(),
    val username: String = "",
    val password: String = "",
)
