package com.reservation.ticket.concert.infrastructure.exception

import com.reservation.ticket.concert.domain.ErrorCode

class UserNotFoundException : RuntimeException(ErrorCode.USER_NOT_FOUND.message){
}