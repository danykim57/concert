package com.reservation.ticket.concert.interfaces.api.token

import com.reservation.ticket.concert.domain.dto.request.TokenRequest
import com.reservation.ticket.concert.domain.dto.response.CommonResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController(value = "/api/token")
class TokenController {

    @PostMapping
    fun getToken(@RequestBody req: TokenRequest, response: HttpServletResponse): CommonResponse {
        val token = "sample-token"
        response.setHeader("Authorization", "Bearer $token")

        return CommonResponse(
            code = "success",
            message = "Token successfully generated",
        )
    }
}