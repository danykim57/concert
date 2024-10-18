package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime

class ConcertServiceTest {

    @Mock
    private lateinit var concertRepository: ConcertRepository

    @InjectMocks
    private lateinit var concertService: ConcertService

    init {
        MockitoAnnotations.openMocks(this) // Mockito 초기화
    }

    @Test
    fun `getAvailableConcerts should return list of available concerts`() {
        // Given
        val now = LocalDateTime.now()
        val concerts = listOf(
            Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = now, availableTickets = 48),
            Concert(id = 2L, name = "Concert 2", location = "Austin City Limits", date = now, availableTickets = 48)
        )

        // Mocking concertRepository to return a list of available concerts
        `when`(concertRepository.findAvailableConcerts()).thenReturn(concerts)

        // When
        val result = concertService.getAvailableConcerts()

        // Then
        assertNotNull(result)
        assertEquals(2, result.size)
        assertTrue(result.all { it.availableTickets > 0 })
        verify(concertRepository, times(1)).findAvailableConcerts()
    }

    @Test
    fun `getAvailableConcerts should return empty list when no concerts are available`() {
        // Given
        val emptyConcertList = emptyList<Concert>()

        // Mocking concertRepository to return an empty list
        `when`(concertRepository.findAvailableConcerts()).thenReturn(emptyConcertList)

        // When
        val result = concertService.getAvailableConcerts()

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())  // 콘서트 목록이 빈 리스트인지 확인
        verify(concertRepository, times(1)).findAvailableConcerts()  // findAvailableConcerts 메서드 호출 확인
    }

}