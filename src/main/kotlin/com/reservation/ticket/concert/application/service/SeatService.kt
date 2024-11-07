package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import com.reservation.ticket.concert.infrastructure.annotation.DistributedLock
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SeatService(
    private val seatRepository: SeatRepository,
    private val concertRepository: ConcertRepository,
    private val cacheManager: RedisCacheManager,
    ) {

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

        val cache = cacheManager.getCache("seat")
        val cachedSeat = cache?.get("seat", Seat::class.java)

        if (cachedSeat != null) {
            return cachedSeat
        }
        val resultSeat =  seatRepository.findWriteLockById(seatId).orElseThrow {
            throw IllegalArgumentException("해당 좌석이 존재하지 않습니다.")
        }

        cache?.put("seat", resultSeat)

        return resultSeat
    }

    fun save(seat: Seat): Seat {
        val savedSeat = seatRepository.save(seat)
        val cache = cacheManager.getCache("seat")
        cache?.put("seat", savedSeat)
        return savedSeat
    }

    fun createSeatForTest(): Seat {
        // 예시로 기본 사용 가능 좌석 생성

        val concert = concertRepository.save(
            Concert(
                name = "Test Concert",
                availableTickets = 50,
                location = "Chicago",
                date = LocalDateTime.of(2040, 12, 31, 19, 0),
            )
        )
        val seat = Seat(
            seatNumber = "A1",
            isAvailable = true,
            concert = concert,
            price = 10.0
        )
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

    @DistributedLock(lockKey = "seatKey", timeout = 5000)
    fun saveWithDistributedLock(seatId: Long): Seat {
        val seat = get(seatId)
            ?: throw UnprocessableEntityException("해당 좌석이 존재하지 않습니다.")


        if (!seat.isAvailable) {
            throw UnprocessableEntityException("해당 좌석은 이미 예약되었습니다.")
        }

        seat.isAvailable = false
        val resultSeat = save(seat)
        val cache = cacheManager.getCache("seat")
        cache?.put("seat", resultSeat)
        return resultSeat
    }

}