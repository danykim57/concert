package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Concert
import org.springframework.stereotype.Service
import java.util.*

@Service
class ConcertFacade (
    private val concertService: ConcertService,
    private val queueStatusChecker: QueueStatusChecker,
){
    fun getAvailableConcerts(userId: UUID): List<Concert> {
        if (queueStatusChecker.isQueueStatusPass(userId) == false)
            throw IllegalArgumentException("아직 대기중입니다.")
        return concertService.getAvailableConcerts()
    }
}