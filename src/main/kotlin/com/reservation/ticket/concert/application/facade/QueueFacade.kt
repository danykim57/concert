package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.interfaces.request.TokenRequest
import com.reservation.ticket.concert.interfaces.response.TokenResponse
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody

@Service
class QueueFacade(
    private val userService: UserService,
    private val concertService: ConcertService,
    private val queueService: QueueService,
) {

    fun getToken(@RequestBody req: TokenRequest): TokenResponse {
        val user = userService.get(req.id, req.password)
        val concert = concertService.get(req.concertCode)
        val token = queueService.createQueue(user, concert)
        return TokenResponse(
            code = "success",
            token = token,
        )
    }
}