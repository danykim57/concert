package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.interfaces.request.PointRequest

class UserFacade(
    private val userService: UserService,
    private val queueStatusChecker: QueueStatusChecker,
) {
    fun add(request: PointRequest) {
        if (queueStatusChecker.isQueueStatusPass(request.userId) == false)
            throw IllegalArgumentException("아직 대기중입니다.")
        userService.add(request)
    }
}