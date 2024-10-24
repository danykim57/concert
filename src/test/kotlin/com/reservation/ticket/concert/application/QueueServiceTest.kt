package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.domain.*
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.infrastructure.QueueRepository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any

import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`

import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class QueueServiceTest {

    @Mock
    lateinit var queueRepository: QueueRepository

    @InjectMocks
    lateinit var queueService: QueueService

    @Test
    fun `createQueue - successfully generate a new queue`() {
        // given
        val user = User(id = UUID.randomUUID(), username = "tester", password = "password")
        val concert = Concert(id = 1L, name = "Test Concert", availableTickets = 50 , location = "Chicago", date = LocalDateTime.now())
        val newQueue = Queue(
            id = 1L,
            userId = user.id,
            status = QueueStatus.WAITING,
            concert = concert,
            token = UUID.randomUUID(),
        )
        // when
        `when`(queueRepository.findByUserIdAndConcert(user.id, concert)).thenReturn(null)
        `when`(queueRepository.save(anyOrNull())).thenReturn(newQueue)

        // then
        val result = queueService.createQueue(user, concert)

        assertNotNull(result)
        assertEquals(newQueue.token.toString(), result)
        verify(queueRepository).save(anyOrNull())
    }

    @Test
    fun `createQueue - return the existing queue size when the queue status is waiting`() {
        // given
        val user = User(id = UUID.randomUUID(), username = "tester", password = "password")
        val concert = Concert(id = 1L, name = "Test Concert", availableTickets = 50 , location = "Chicago", date = LocalDateTime.now())
        val existingQueue = Queue(
            id = 1L,
            userId = user.id,
            status = QueueStatus.WAITING,
            concert = concert,
            token = UUID.randomUUID()
        )
        val existingQueue2 = Queue(
            id = 2L,
            userId = user.id,
            status = QueueStatus.WAITING,
            concert = concert,
            token = UUID.randomUUID()
        )
        // when
        `when`(queueRepository.findByUserIdAndConcert(user.id, concert)).thenReturn(existingQueue)
        `when`(queueRepository.findAllByConcertAndStatus(concert, QueueStatus.WAITING)).thenReturn(listOf(existingQueue, existingQueue2))

        // then
        val result = queueService.createQueue(user, concert)

        assertTrue(result.contains("대기 순번은"))
        verify(queueRepository, never()).save(anyOrNull())
    }

    @Test
    fun `createQueue - the queue already exists and status is not waiting then update the queue status`() {
        // given
        val user = User(id = UUID.randomUUID(), username = "tester", password = "password")
        val concert = Concert(id = 1L, name = "Test Concert", availableTickets = 50 , location = "Chicago", date = LocalDateTime.now())
        val existingQueue = Queue(
            id = 1L,
            userId = user.id,
            status = QueueStatus.PASS,
            concert = concert,
            token = UUID.randomUUID()
        )
        // when
        `when`(queueRepository.findByUserIdAndConcert(user.id, concert)).thenReturn(existingQueue)
        `when`(queueRepository.save(existingQueue)).thenReturn(existingQueue)

        // then
        val result = queueService.createQueue(user, concert)

        assertNotNull(result)
        assertEquals(existingQueue.token.toString(), result)
        verify(queueRepository).save(existingQueue)
    }
}