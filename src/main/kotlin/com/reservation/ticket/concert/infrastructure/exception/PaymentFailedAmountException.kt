package com.reservation.ticket.concert.infrastructure.exception

import com.reservation.ticket.concert.domain.ErrorCode

class PaymentFailedAmountException: RuntimeException(ErrorCode.PAYMENT_FAILED_AMOUNT.message) {
}