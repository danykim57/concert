package com.reservation.ticket.concert.interfaces.api.token

import com.reservation.ticket.concert.application.facade.QueueFacade
import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.interfaces.request.TokenRequest
import com.reservation.ticket.concert.interfaces.response.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController(value = "/api/token")
class TokenController(
    private val queueFacade: QueueFacade
) {

    @Operation(summary = "토큰 발급 신청", description = "토큰 발급 신청 API")
    @PostMapping
    fun getToken(@RequestBody req: TokenRequest): TokenResponse {
        return queueFacade.getToken(req)
    }

}