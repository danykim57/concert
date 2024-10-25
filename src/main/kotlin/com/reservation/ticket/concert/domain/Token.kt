package com.reservation.ticket.concert.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class Token(
    @Id
    val token: String = UUID.randomUUID().toString(), // 토큰은 UUID로 생성
    @ManyToOne
    val user: User
)
