package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class SeatServiceIntegrationTest(
    @Autowired val seatService: SeatService,
    @Autowired val seatRepository: SeatRepository,
    @Autowired val concertRepository: ConcertRepository
) {
    @BeforeEach
    fun setup() {
        // 콘서트 생성
        val concert = concertRepository.save(
            Concert(
                name = "Test Concert",
                availableTickets = 50,
                location = "Chicago",
                date = LocalDateTime.of(2040, 12, 31, 19, 0),
            )
        )

        // 좌석 1 생성 및 저장
        val seat1 = Seat(
            seatNumber = "A1",
            isAvailable = true,
            concert = concert,
            price = 200.0
        )
        seatRepository.save(seat1)

        // 좌석 2 생성 및 저장
        val seat2 = Seat(
            seatNumber = "A2",
            isAvailable = true,
            concert = concert,
            price = 200.0
        )
        seatRepository.save(seat2)
    }

    @Test
    fun getAvailableSeats() {
        val date = LocalDateTime.of(2050, 12, 31, 23, 59)
        val seats = seatService.getAvailableSeats(date)
        println("size: " + seats.size)
        assertTrue(seats.isNotEmpty())
    }
}