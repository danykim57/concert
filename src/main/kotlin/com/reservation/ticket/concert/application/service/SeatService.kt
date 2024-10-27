package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.infrastructure.SeatRepository
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SeatService(private val seatRepository: SeatRepository) {

    // 예약 날짜에 해당하는 예약 가능한 좌석 목록을 조회
    @Transactional
    fun getAvailableSeats(date: LocalDateTime): List<Seat> {
        return seatRepository.findByConcertDateBetween(LocalDateTime.now(), date)
    }

    fun get(seatId: Long): Seat? {
        return seatRepository.findById(seatId).orElseThrow {
            throw IllegalArgumentException("해당 좌석이 존재하지 않습니다.")
        }
    }

    fun getWithLock(seatId: Long): Seat? {
        return seatRepository.findWriteLockById(seatId).orElseThrow {
            throw IllegalArgumentException("해당 좌석이 존재하지 않습니다.")
        }
    }

    fun save(seat: Seat): Seat {
        return seatRepository.save(seat)
    }

    fun save(seatId: Long): Seat {
        val seat = getWithLock(seatId)
            ?: throw UnprocessableEntityException("해당 좌석이 존재하지 않습니다.")


        if (!seat.isAvailable) {
            throw UnprocessableEntityException("해당 좌석은 이미 예약되었습니다.")
        }

        seat.isAvailable = false
        return save(seat)
    }

}