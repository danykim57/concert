package com.reservation.ticket.concert.infrastructure.aop

import com.reservation.ticket.concert.domain.ErrorCode
import com.reservation.ticket.concert.infrastructure.exception.AfterDeadlineException
import com.reservation.ticket.concert.infrastructure.exception.PaymentFailedAmountException
import com.reservation.ticket.concert.infrastructure.exception.PaymentTimeOutException
import com.reservation.ticket.concert.infrastructure.exception.UnAuthorizedException
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import com.reservation.ticket.concert.infrastructure.exception.UserNotFoundException
import com.reservation.ticket.concert.interfaces.response.CommonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.client.HttpClientErrorException.Forbidden


@RestControllerAdvice
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
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            code = HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase,
            message = ex.message!!
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleCustomException(ex: IllegalArgumentException): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message!!
        )
    }

    @ExceptionHandler(BadRequest::class)
    fun handleCustomException(ex: BadRequest): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message!!
        )
    }

    @ExceptionHandler(Forbidden::class)
    fun handleCustomException(ex: Forbidden): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = ErrorCode.FORBIDDEN.statusCode.value(),
            code = ErrorCode.FORBIDDEN.message,
            message = ex.message!!
        )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleCustomException(ex: UserNotFoundException): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = ErrorCode.USER_NOT_FOUND.statusCode.value(),
            code = ErrorCode.USER_NOT_FOUND.message,
            message = ex.message!!
        )
    }

    @ExceptionHandler(AfterDeadlineException::class)
    fun handleCustomException(ex: AfterDeadlineException): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = ErrorCode.AFTER_DEADLINE.statusCode.value(),
            code = ErrorCode.AFTER_DEADLINE.message,
            message = ex.message!!
        )
    }

    @ExceptionHandler(PaymentTimeOutException::class)
    fun handleCustomException(ex: PaymentTimeOutException): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = ErrorCode.PAYMENT_TIMEOUT.statusCode.value(),
            code = ErrorCode.PAYMENT_TIMEOUT.message,
            message = ex.message!!
        )
    }

    @ExceptionHandler(PaymentFailedAmountException::class)
    fun handleCustomException(ex: PaymentFailedAmountException): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = ErrorCode.PAYMENT_FAILED_AMOUNT.statusCode.value(),
            code = ErrorCode.PAYMENT_FAILED_AMOUNT.message,
            message = ex.message!!
        )
    }

    @ExceptionHandler(UnAuthorizedException::class)
    fun handleCustomException(ex: UnAuthorizedException): CommonResponse {
        logger.warn(ex.message, ex)

        return CommonResponse(
            status = ErrorCode.UNAUTHORIZED.statusCode.value(),
            code = ErrorCode.UNAUTHORIZED.message,
            message = ex.message!!
        )
    }

}