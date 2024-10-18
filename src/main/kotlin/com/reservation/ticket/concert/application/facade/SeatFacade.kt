package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.infrastructure.SeatRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class SeatFacade (
    private val seatService: SeatService,
    private val queueStatusChecker: QueueStatusChecker,
) {

    fun getAvailableSeats(date:LocalDateTime, userId: UUID): List<Seat>{
        if (queueStatusChecker.isQueueStatusPass(userId) == false)
            throw IllegalArgumentException("아직 대기중입니다.")
        return seatService.getAvailableSeats(date)
    }

}