package com.reservation.ticket.concert.infrastructure

import com.reservation.ticket.concert.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PaymentRepository : JpaRepository<Payment, Long> {
    override fun findById(id: Long): Optional<Payment>
    fun save(payment: Payment): Payment
}