package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.facade.QueueFacade
import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.interfaces.request.TokenRequest
import com.reservation.ticket.concert.interfaces.response.TokenResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
class QueueFacadeIntegrationTest {

    @Autowired
    private lateinit var queueFacade: QueueFacade

    @MockBean
    private lateinit var userService: UserService

    @MockBean
    private lateinit var concertService: ConcertService

    @MockBean
    private lateinit var queueService: QueueService

    private lateinit var testUser: User
    private lateinit var testConcert: Concert
    private lateinit var tokenRequest: TokenRequest

    @BeforeEach
    fun setup() {
        // 테스트용 유저, 콘서트, 요청 객체 생성
        testUser = User(
            id = UUID.randomUUID(),
            password = "password123"
        )

        testConcert = Concert(
            id = 1L,
            name = "name",
            location = "Chicago",
            date = LocalDateTime.now(),
            availableTickets = 50
        )

        tokenRequest = TokenRequest(
            id = testUser.username,
            password = "password123",
            concertCode = "TEST_CONCERT_CODE"
        )

        // Mocking UserService
        `when`(userService.get(testUser.username, tokenRequest.password))
            .thenReturn(testUser)

        // Mocking ConcertService
        `when`(concertService.get(tokenRequest.concertCode))
            .thenReturn(testConcert)

        // Mocking QueueService
        `when`(queueService.createQueue(testUser, testConcert))
            .thenReturn("dummyToken")
    }

    @Test
    fun `getToken should return success token response`() {
        // 실행
        val response: TokenResponse = queueFacade.getToken(tokenRequest)

        // 검증
        assertEquals("success", response.code)
        assertEquals("dummyToken", response.token)

        // 각 서비스가 올바르게 호출되었는지 검증
        verify(userService).get(testUser.username, tokenRequest.password)
        verify(concertService).get(tokenRequest.concertCode)
        verify(queueService).createQueue(testUser, testConcert)
    }

    @Test
    fun `getToken should throw exception when user is not found`() {
        // 유저가 없을 때의 동작을 정의
        `when`(userService.get(testUser.username, tokenRequest.password))
            .thenThrow(IllegalArgumentException("User not found"))

        // 예외 발생 여부 확인
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queueFacade.getToken(tokenRequest)
        }

        assertEquals("User not found", exception.message)

        // 검증: 유저를 찾지 못한 경우 콘서트와 큐 서비스가 호출되지 않아야 함
        verify(concertService, never()).get(anyString())
        verify(queueService, never()).createQueue(anyOrNull(), anyOrNull())
    }

    @Test
    fun `getToken should throw exception when concert is not found`() {
        // 콘서트를 찾지 못할 때의 동작을 정의
        `when`(concertService.get(tokenRequest.concertCode))
            .thenThrow(IllegalArgumentException("Concert not found"))

        // 예외 발생 여부 확인
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queueFacade.getToken(tokenRequest)
        }

        assertEquals("Concert not found", exception.message)

        // 검증: 콘서트를 찾지 못한 경우 큐 서비스가 호출되지 않아야 함
        verify(queueService, never()).createQueue(anyOrNull(), anyOrNull())
    }
}
