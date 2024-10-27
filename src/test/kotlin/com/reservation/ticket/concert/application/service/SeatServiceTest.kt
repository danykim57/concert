package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.infrastructure.SeatRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class SeatServiceTest {

    @Mock
    private lateinit var seatRepository: SeatRepository

    @InjectMocks
    private lateinit var seatService: SeatService

    @Test
    fun `should return available seats for the given date`() {
        // Given
        val now = LocalDateTime.now()
        val date = now.plusDays(7) // 예약 가능한 날짜로 7일 후를 설정
        val concerts = listOf(
            Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = now, availableTickets = 48),
            Concert(id = 2L, name = "Concert 2", location = "Austin City Limits", date = now, availableTickets = 48)
        )
        val availableSeats = listOf(
            Seat(id = 1L, seatNumber = "A1", isAvailable = true, concert = concerts.get(0), price = 10.0),
            Seat(id = 2L, seatNumber = "A2", isAvailable = true, concert = concerts.get(1), price = 10.0)
        )

        // Mocking the repository behavior
        `when`(seatRepository.findByConcertDateBetween(anyOrNull(), anyOrNull())).thenReturn(availableSeats)

        // When
        val result = seatService.getAvailableSeats(date)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isAvailable })  // 모든 좌석이 예약 가능한 상태인지 확인
        assertEquals("A1", result[0].seatNumber)  // 첫 번째 좌석이 "A1"인지 확인
        assertEquals("A2", result[1].seatNumber)  // 두 번째 좌석이 "A2"인지 확인
    }

    @Test
    fun `should return empty list if no seats are available`() {
        // Given
        val now = LocalDateTime.now()
        val date = now.plusDays(7)

        // Mocking the repository behavior to return an empty list
        `when`(seatRepository.findByConcertDateBetween(anyOrNull(), anyOrNull())).thenReturn(emptyList())

        // When
        val result = seatService.getAvailableSeats(date)

        // Then
        assertTrue(result.isEmpty())  // 좌석이 없으면 빈 리스트가 반환되어야 함
    }
}