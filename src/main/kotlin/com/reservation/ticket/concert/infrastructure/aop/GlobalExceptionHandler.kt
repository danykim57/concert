package com.reservation.ticket.concert.infrastructure.aop

import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import com.reservation.ticket.concert.interfaces.response.CommonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.client.HttpClientErrorException.Forbidden


@RestController
class GlobalExceptionHandler {

    val logger: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(ex: Exception): CommonResponse {
        logger.error(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = ex.message!!
        )
    }

    @ExceptionHandler(UnprocessableEntityException::class)
    fun handleCustomException(ex: UnprocessableEntityException): CommonResponse {
        logger.error(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            code = HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase,
            message = ex.message!!
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleCustomException(ex: IllegalArgumentException): CommonResponse {
        logger.error(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message!!
        )
    }

    @ExceptionHandler(BadRequest::class)
    fun handleCustomException(ex: BadRequest): CommonResponse {
        logger.error(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message!!
        )
    }

    @ExceptionHandler(Forbidden::class)
    fun handleCustomException(ex: Forbidden): CommonResponse {
        logger.error(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.FORBIDDEN.value(),
            code = HttpStatus.FORBIDDEN.reasonPhrase,
            message = ex.message!!
        )
    }

}