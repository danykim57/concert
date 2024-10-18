package com.reservation.ticket.concert.application.facade

import org.junit.jupiter.api.Assertions.*

import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import com.reservation.ticket.concert.interfaces.request.BookingRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
class BookingFacadeTest(
    @Autowired private val bookingFacade: BookingFacade,
    @Autowired private val seatRepository: SeatRepository,
    @Autowired private val concertRepository: ConcertRepository,
    @Autowired private val queueRepository: QueueRepository,
    @Autowired private val queueStatusChecker: QueueStatusChecker
) {

    private lateinit var concert: Concert
    private lateinit var seat: Seat
    private lateinit var user: User
    private lateinit var userId: UUID
    private lateinit var now: LocalDateTime

    @BeforeEach
    fun setUp() {
        // 콘서트 및 좌석을 테스트용으로 세팅
        userId = UUID.randomUUID()
        user = User(id = userId, username = "testUser", password = "password")
        now = LocalDateTime.now()
        concert = concertRepository.save(
            Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = now, availableTickets = 48),
        )

        seat = seatRepository.save(
            Seat(
                seatNumber = "A1",
                isAvailable = true,
                concert = concert,
                price = 10.0
            )
        )
    }

    @Test
    @Transactional
    fun `유효한 요청으로 좌석 예약이 성공해야 한다`() {
        // Given: 유효한 대기열 상태를 설정
        `when`(queueStatusChecker.isQueueStatusPass(userId)).thenReturn(true)

        // 예약 요청 생성
        val request = BookingRequest(
            concertId = concert.id,
            seatId = seat.id,
            queueToken = "validQueueToken"
        )

        // When: 좌석 예약
        val seatDTO = bookingFacade.book(request, userId)

        // Then: 예약된 좌석이 반환되어야 한다
        assertNotNull(seatDTO)
        assertEquals(seat.id, seatDTO.id)
        assertFalse(seatDTO.isAvailable)  // 좌석 상태는 예약된 상태여야 한다
    }

    @Test
    @Transactional
    fun `유효하지 않은 토큰으로 예약 시 실패해야 한다`() {
        // Given: 유효하지 않은 대기열 토큰
        `when`(queueStatusChecker.isQueueStatusPass(userId)).thenReturn(true)

        val request = BookingRequest(
            concertId = concert.id,
            seatId = seat.id,
            queueToken = "invalidQueueToken"
        )

        // When / Then: 유효하지 않은 토큰일 때 예외가 발생해야 한다
        val exception = assertThrows<IllegalArgumentException> {
            bookingFacade.book(request, userId)
        }

        assertEquals("유효하지 않은 대기열 토큰입니다.", exception.message)
    }

    @Test
    @Transactional
    fun `대기열 상태가 PASS가 아닌 경우 예약 시 실패해야 한다`() {
        // Given: 대기열 상태가 PASS가 아닌 경우
        `when`(queueStatusChecker.isQueueStatusPass(userId)).thenReturn(false)

        val request = BookingRequest(
            concertId = concert.id,
            seatId = seat.id,
            queueToken = "validQueueToken"
        )

        // When / Then: 대기중일 때 예외가 발생해야 한다
        val exception = assertThrows<IllegalArgumentException> {
            bookingFacade.book(request, userId)
        }

        assertEquals("아직 대기중입니다.", exception.message)
    }

    @Test
    @Transactional
    fun `좌석이 이미 예약된 경우 예약 시 실패해야 한다`() {
        // Given: 좌석이 이미 예약된 상태
        seat.isAvailable = false
        seatRepository.save(seat)

        `when`(queueStatusChecker.isQueueStatusPass(userId)).thenReturn(true)

        val request = BookingRequest(
            concertId = concert.id,
            seatId = seat.id,
            queueToken = "validQueueToken"
        )

        // When / Then: 좌석이 이미 예약된 경우 예외가 발생해야 한다
        val exception = assertThrows<IllegalStateException> {
            bookingFacade.book(request, userId)
        }

        assertEquals("해당 좌석은 이미 예약되었습니다.", exception.message)
    }
}
