package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.domain.*
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.infrastructure.QueueRepository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class QueueServiceTest {

    @Mock
    private lateinit var queueRepository: QueueRepository

    @InjectMocks
    private lateinit var queueService: QueueService

    private lateinit var user: User

    private lateinit var concert: Concert

    init {
        MockitoAnnotations.openMocks(this)
    }

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        user = User(id = UUID.randomUUID(), username = "testuser", password = "password")
        concert = Concert(id = 1L, name = "Test Concert", availableTickets = 50 , location = "Chicago", date = LocalDateTime.now())
    }

    @Test
    fun `should create new queue and return token when no existing queue`() {
        // Given
        val newQueue = Queue(
            id = 1L,
            userId = user.id,
            status = QueueStatus.WAITING,
            concert = concert,
            token = UUID.randomUUID()
        )

        `when`(queueRepository.findByUserIdAndConcert(user.id, concert)).thenReturn(newQueue)
        `when`(queueRepository.save(anyOrNull())).thenReturn(newQueue)

        // When
        val result = queueService.createQueue(user, concert)

        // Then
        assertEquals(newQueue.token.toString(), result)
        verify(queueRepository, times(1)).save(anyOrNull())
    }

    @Test
    fun `should return waiting position if queue status is WAITING`() {
        // Given
        val existingQueue = Queue(
            id = 1L,
            userId = user.id,
            status = QueueStatus.WAITING,
            concert = concert,
            token = UUID.randomUUID()
        )

        `when`(queueRepository.findByUserIdAndConcert(user.id, concert)).thenReturn(existingQueue)

        val waitingQueues = ArrayList<Queue>()
        waitingQueues.add(existingQueue)

        `when`(queueRepository.findAllByConcertAndStatus(concert, QueueStatus.WAITING)).thenReturn(waitingQueues)

        // When
        val result = queueService.createQueue(user, concert)

        // Then
        assertEquals("대기 순번은 $waitingQueues 입니다.", result)
        verify(queueRepository, never()).save(anyOrNull())
    }

    @Test
    fun `should create new queue with PASS status if existing queue status is not WAITING`() {
        // Given
        val existingQueue = Queue(
            id = 1L,
            userId = user.id,
            status = QueueStatus.PASS,
            concert = concert,
            token = UUID.randomUUID()
        )

        `when`(queueRepository.findByUserIdAndConcert(user.id, concert)).thenReturn(existingQueue)

        val newQueue = Queue(
            id = 2L,
            userId = user.id,
            status = QueueStatus.PASS,
            concert = concert,
            token = UUID.randomUUID()
        )

        `when`(queueRepository.save(anyOrNull())).thenReturn(newQueue)

        // When
        val result = queueService.createQueue(user, concert)

        // Then
        assertEquals(newQueue.token.toString(), result)
        verify(queueRepository, times(1)).save(anyOrNull())
    }

}