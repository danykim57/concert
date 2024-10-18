package com.reservation.ticket.concert.application.facade

import org.junit.jupiter.api.Assertions.*
import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.interfaces.request.TokenRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class QueueFacadeTest {

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var concertService: ConcertService

    @Mock
    private lateinit var queueService: QueueService

    @InjectMocks
    private lateinit var queueFacade: QueueFacade

    @Test
    fun `should return token when user and concert exist`() {
        // Given
        val req = TokenRequest(id = "user123", password = "password123", concertCode = "concert123")
        val user = User()
        val concert = Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = LocalDateTime.now(), availableTickets = 48)
        val expectedToken = "some-random-token"

        // Mocking the behavior of services
        `when`(userService.get(req.id, req.password)).thenReturn(user)
        `when`(concertService.get(req.concertCode)).thenReturn(concert)
        `when`(queueService.createQueue(user, concert)).thenReturn(expectedToken)

        // When
        val response = queueFacade.getToken(req)

        // Then
        assertEquals("success", response.code)
        assertEquals(expectedToken, response.token)
    }
}