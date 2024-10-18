package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.infrastructure.SeatRepository
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SeatServiceTest {
    @Mock
    private lateinit var seatRepository: SeatRepository

    @InjectMocks
    private lateinit var seatService: SeatService

    init {
        MockitoAnnotations.openMocks(this) // Mockito 초기화
    }

}