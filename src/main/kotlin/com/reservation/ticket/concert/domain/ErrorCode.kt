package com.reservation.ticket.concert.domain

import org.springframework.http.HttpStatus

enum class ErrorCode(statusCode: HttpStatus, val message: String) {
    HTTP_MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "유효하지 않은 타입이거나 요청 값이 누락되었습니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 값이 존재하지 않습니다."),

    // Business Error
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "토큰 인증에 실패하였습니다."),
    BEFORE_RESERVATION_AT(HttpStatus.BAD_REQUEST, "예약하기에는 이릅니다."),
    AFTER_DEADLINE(HttpStatus.BAD_REQUEST, "예약 가능 시간이 지났습니다."),
    SEAT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "예약 가능한 좌석이 아닙니다."),
    PAYMENT_TIMEOUT(HttpStatus.BAD_REQUEST, "결제 가능한 시간이 지났습니다."),
    PAYMENT_DIFFERENT_USER(HttpStatus.BAD_REQUEST, "결제자 정보가 불일치합니다."),
    PAYMENT_FAILED_AMOUNT(HttpStatus.BAD_REQUEST, "결제 잔액이 부족합니다."),
}