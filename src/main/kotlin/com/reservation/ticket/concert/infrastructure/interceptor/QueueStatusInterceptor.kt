package com.reservation.ticket.concert.infrastructure.interceptor

import com.reservation.ticket.concert.application.token.QueueStatusChecker
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.coyote.BadRequestException
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.UUID

@Component
class QueueStatusInterceptor(
    private val queueStatusChecker: QueueStatusChecker
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // 요청 헤더나 파라미터에서 토큰과 userId 추출
        val token = request.getHeader("Authorization") ?: ""
        val userIdString = request.getParameter("userId") ?: throw BadRequestException("User ID is missing")

        val userId = try {
            UUID.fromString(userIdString)
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("Invalid User ID format")
        }


        queueStatusChecker.checkTokenValid(token, userId)

        return true
    }
}