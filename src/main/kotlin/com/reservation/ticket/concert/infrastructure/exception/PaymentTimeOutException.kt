package com.reservation.ticket.concert.infrastructure.exception

import com.reservation.ticket.concert.domain.ErrorCode

class PaymentTimeOutException: RuntimeException(ErrorCode.PAYMENT_TIMEOUT.message) {
}