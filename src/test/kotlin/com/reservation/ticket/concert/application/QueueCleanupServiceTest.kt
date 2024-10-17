package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.schedule.QueueCleanupService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.infrastructure.QueueRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime
import java.util.*

class QueueCleanupServiceTest {
    @Mock
    private lateinit var queueRepository: QueueRepository

    @InjectMocks
    private lateinit var queueCleanupService: QueueCleanupService

    @BeforeEach
    fun setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should delete expired queues`() {
        // Given
        val now = LocalDateTime.now()
        val concert = Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = now, availableTickets = 48)
        val token = UUID.randomUUID()
        // Mock된 만료된 Queue 리스트 생성 (status: PASS, updatedAt 11분 전)
        val expiredQueue1 = Queue(
            id = 1L,
            userId = UUID.randomUUID(),
            status = QueueStatus.PASS,
            concert = concert,
            token = token,
            createdAt = now.minusMinutes(15),
            updatedAt = now.minusMinutes(11),
        )

        val expiredQueue2 = Queue(
            id = 2L,
            userId = UUID.randomUUID(),
            status = QueueStatus.PASS,
            concert = concert,
            token = token,
            createdAt = now.minusMinutes(20),
            updatedAt = now.minusMinutes(12)
        )

        // Mock된 Queue 목록 반환
        `when`(queueRepository.findAll()).thenReturn(listOf(expiredQueue1, expiredQueue2))

        // When
        queueCleanupService.cleanUpOldQueues()

        // Then
        verify(queueRepository, times(1)).deleteAll(listOf(expiredQueue1, expiredQueue2))
    }

    @Test
    fun `should not delete queues that are not expired`() {
        // Given
        val now = LocalDateTime.now()
        val concert = Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = now, availableTickets = 48)

        // Mock된 Queue 리스트 생성 (status: PASS, updatedAt이 9분 전, 만료되지 않음)
        val nonExpiredQueue = Queue(
            id = 3L,
            userId = UUID.randomUUID(),
            status = QueueStatus.PASS,
            concert = concert,
            token = UUID.randomUUID(),
            createdAt = now.minusMinutes(5),
            updatedAt = now.minusMinutes(9)
        )

        // Mock된 Queue 목록 반환
        `when`(queueRepository.findAll()).thenReturn(listOf(nonExpiredQueue))

        // When
        queueCleanupService.cleanUpOldQueues()

        // Then
        verify(queueRepository, times(0)).deleteAll(anyList())
    }

    @Test
    fun `should not delete queues with status not PASS`() {
        // Given
        val now = LocalDateTime.now()
        val concert = Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = now, availableTickets = 48)

        // Mock된 Queue 리스트 생성 (status: NOT_PASS, updatedAt이 12분 전)
        val nonPassQueue = Queue(
            id = 4L,
            userId = UUID.randomUUID(),
            status = QueueStatus.WAITING,
            concert = concert,
            token = UUID.randomUUID(),
            createdAt = now.minusMinutes(15),
            updatedAt = now.minusMinutes(12)
        )

        // Mock된 Queue 목록 반환
        `when`(queueRepository.findAll()).thenReturn(listOf(nonPassQueue))

        // When
        queueCleanupService.cleanUpOldQueues()

        // Then
        verify(queueRepository, times(0)).deleteAll(anyList())
    }
}