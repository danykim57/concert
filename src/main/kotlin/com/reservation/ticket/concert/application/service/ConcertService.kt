package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ConcertService(
    private val concertRepository: ConcertRepository,
) {
    fun get(name: String): Concert = concertRepository.findByName(name)
        ?: throw UnprocessableEntityException("유효하지 않은 콘서트 입니다.")

    fun get(id: Long): Optional<Concert> = concertRepository.findById(id)

    fun create(concert: Concert): Concert = concertRepository.save(concert)

    fun getAvailableConcerts(): List<Concert> {
        // 예약 가능한 콘서트를 리포지토리에서 조회
        return concertRepository.findAvailableConcerts()
    }

}