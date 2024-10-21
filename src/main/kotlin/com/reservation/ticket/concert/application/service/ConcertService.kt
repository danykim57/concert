package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ConcertService(
    private val concertRepository: ConcertRepository,
    private val queueStatusChecker: QueueStatusChecker,
) {
    fun get(name: String): Concert = concertRepository.findByName(name)
        ?: throw IllegalArgumentException("유효하지 않은 콘서트 입니다.")

    fun get(id: Long): Optional<Concert> = concertRepository.findById(id)

    fun getAvailableConcerts(): List<Concert> {
        // 예약 가능한 콘서트를 리포지토리에서 조회
        return concertRepository.findAvailableConcerts()
    }

}