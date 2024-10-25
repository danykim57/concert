package com.reservation.ticket.concert.infrastructure.exception

import com.reservation.ticket.concert.domain.ErrorCode

class UnAuthorizedException: RuntimeException(ErrorCode.UNAUTHORIZED.message) {
}