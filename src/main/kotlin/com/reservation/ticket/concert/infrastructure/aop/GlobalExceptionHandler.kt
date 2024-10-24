package com.reservation.ticket.concert.infrastructure.aop

import com.reservation.ticket.concert.interfaces.response.CommonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
class GlobalExceptionHandler {

    val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // 예외 처리 핸들러
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Exception): CommonResponse {

        return CommonResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = ex.message!!
        )
    }

    // Custom Exception 처리 예시
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleCustomException(ex: IllegalArgumentException): CommonResponse {

        return CommonResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message!!
        )
    }

}