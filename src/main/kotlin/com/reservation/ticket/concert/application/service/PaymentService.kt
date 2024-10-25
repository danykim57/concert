package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Payment
import com.reservation.ticket.concert.infrastructure.PaymentRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {

    fun save(payment: Payment): Payment {
        return paymentRepository.save(payment)
    }
}