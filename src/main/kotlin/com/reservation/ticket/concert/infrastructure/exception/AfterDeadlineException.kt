package com.reservation.ticket.concert.infrastructure.exception

import com.reservation.ticket.concert.domain.ErrorCode

class AfterDeadlineException : RuntimeException(ErrorCode.AFTER_DEADLINE.message) {
}