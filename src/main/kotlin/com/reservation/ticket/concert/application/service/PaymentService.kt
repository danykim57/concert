package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Payment
import com.reservation.ticket.concert.domain.PaymentType
import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.infrastructure.PaymentRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {

    fun save(payment: Payment): Payment {
        return paymentRepository.save(payment)
    }

    fun save(point: Point, reservation: Reservation) {
        // 결제 정보 저장
        val payment = Payment(
            userId = reservation.userId,
            reservationId = reservation.id,
            amount = point.amount,
            type = PaymentType.SPEND
        )
        // 히스토리 저장
        this.save(payment)
    }
}