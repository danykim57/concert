package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.PaymentService
import com.reservation.ticket.concert.application.service.PointService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.ReservationService
import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.domain.QueueStatus
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertFailsWith


//@ExtendWith(SpringExtension::class)
@SpringBootTest
class ReservationFacadeTest{

    private lateinit var reservationFacade: ReservationFacade
    private lateinit var reservationService: ReservationService
    private lateinit var userService: UserService
    private lateinit var concertService: ConcertService
    private lateinit var seatService: SeatService
    private lateinit var queueService: QueueService
    private lateinit var pointService: PointService
    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setUp() {
        reservationService = mock(ReservationService::class.java)
        userService = mock(UserService::class.java)
        concertService = mock(ConcertService::class.java)
        seatService = mock(SeatService::class.java)
        queueService = mock(QueueService::class.java)
        pointService = mock(PointService::class.java)
        paymentService = mock(PaymentService::class.java)

        // Facade 생성 시 목 객체를 주입
        reservationFacade = ReservationFacade(
            reservationService,
            userService,
            concertService,
            seatService,
            queueService,
            pointService,
            paymentService
        )
    }

    @Test
    fun `test confirmReservation throws exception for invalid token`() {
        // Given
        val reservationId = 1L
        val concert = Concert(id = 1L, name = "Concert", location = "Chicago", date = LocalDateTime.now(), availableTickets = 50 )
        val seat = Seat(id = 1L, price = 100.0, isAvailable = false, seatNumber = "A1", concert = concert)
        val reservation = Reservation(
            id = reservationId,
            userId = UUID.randomUUID(),
            seat = seat,
            concert = concert,
            createdAt = LocalDateTime.now(),
            status = ReservationStatus.RESERVED
        )
        val user = User(id = reservation.userId)

        `when`(reservationService.get(reservationId)).thenReturn(reservation)
        `when`(userService.getUserWithLock(reservation.userId)).thenReturn(user)
        `when`(seatService.get(reservation.seat.id)).thenReturn(seat)

        // When / Then
        val exception = assertFailsWith<IllegalArgumentException> {
            reservationFacade.confirmReservation(reservationId)
        }

        assertEquals("유효하지 않은 토큰 입니다.", exception.message)
    }

    @Test
    fun `should throw exception if reservation does not exist`() {
        // Given
        val reservationId = 1L

        `when`(reservationService.get(anyOrNull())).thenThrow(IllegalArgumentException("해당 예약이 존재하지 않습니다."))

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationFacade.confirmReservation(reservationId)
        }

        assertEquals("해당 예약이 존재하지 않습니다.", exception.message)
    }

    @Test
    fun `should throw exception if user does not exist`() {
        // Given
        val reservation = mockReservation()

        `when`(reservationService.get(anyOrNull())).thenReturn(reservation)
        `when`(userService.getUserWithLock(anyOrNull())).thenThrow(IllegalArgumentException("해당 유저를 찾을 수 없습니다."))

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationFacade.confirmReservation(reservation.id)
        }

        assertEquals("해당 유저를 찾을 수 없습니다.", exception.message)
    }

    @Test
    fun `should cancel reservation if 5 minutes has passed`() {
        // Given
        val reservation = mockReservation(createdAt = LocalDateTime.now().minusMinutes(6))
        val concert = Concert(id = 1L, name = "Concert", location = "Chicago", date = LocalDateTime.now(), availableTickets = 50 )
        val seat = Seat(id = 1L, price = 100.0, isAvailable = false, seatNumber = "A1", concert = concert)
        val queue = mockQueue(userId = reservation.userId)
        val user = User(id = reservation.userId)
        val point = Point(id = 1L, amount =  200.0, user = user)

        `when`(reservationService.get(anyOrNull())).thenReturn(reservation)
        `when`(userService.getUserWithLock(anyOrNull())).thenReturn(mockUser(reservation.userId))
        `when`(concertService.get(anyLong())).thenReturn(Optional.of(reservation.concert))
        `when`(seatService.get(anyOrNull())).thenReturn(seat)
        `when`(queueService.get(anyOrNull())).thenReturn(queue)

        `when`(userService.getPoint(user.id)).thenReturn(point)

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationFacade.confirmReservation(reservation.id)
        }

        assertEquals("만료된 대기열 토큰입니다.", exception.message)
        verify(queueService).delete(queue)
        assertTrue(seat.isAvailable)
        assertEquals(ReservationStatus.CANCELLED, reservation.status)
        verify(seatService).save(seat)
        verify(reservationService).save(reservation)
    }

    @Test
    fun `should throw exception if user has insufficient points`() {
        // Given
        val reservation = mockReservation()
        val user = mockUser(reservation.userId)
        val point = Point(user = user, amount = 50.0) // 부족한 포인트
        val concert = Concert(id = 1L, name = "Concert", location = "Chicago", date = LocalDateTime.now(), availableTickets = 50 )
        val seat = Seat(id = 1L, price = 100.0, isAvailable = false, seatNumber = "A1", concert = concert)

        `when`(reservationService.get(anyOrNull())).thenReturn(reservation)
        `when`(userService.getUserWithLock(anyOrNull())).thenReturn(user)
        `when`(userService.getPoint(anyOrNull())).thenReturn(point)

        `when`(seatService.get(anyOrNull())).thenReturn(seat)

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationFacade.confirmReservation(reservation.id)
        }

        assertEquals("포인트 잔액이 부족합니다.", exception.message)
    }

    @Test
    fun `should confirm reservation if user has sufficient points`() {
        // Given
        val reservation = mockReservation()
        val user = mockUser(reservation.userId)
        val point = Point(user = user, amount = 200.0) // 충분한 포인트
        val concert = Concert(id = 1L, name = "Concert", location = "Chicago", date = LocalDateTime.now(), availableTickets = 50 )
        val seat = Seat(id = 1L, price = 100.0, isAvailable = false, seatNumber = "A1", concert = concert)

        `when`(reservationService.get(anyOrNull())).thenReturn(reservation)
        `when`(userService.getUserWithLock(anyOrNull())).thenReturn(user)
        `when`(userService.getPoint(anyOrNull())).thenReturn(point)
        `when`(pointService.save(anyOrNull())).thenReturn(point)
        `when`(seatService.get(anyOrNull())).thenReturn(seat)
        // When
        val result = reservationFacade.confirmReservation(reservation.id)

        // Then
        assertEquals("예약이 결제 완료 처리되었습니다.", result)
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
        assertEquals(50.0, point.amount) // 포인트가 차감된 상태
        verify(pointService).save(point)
    }

    // Mocking helper functions
    private fun mockReservation(
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Reservation {
        val seat = Seat(id = 1L, seatNumber = "A1", isAvailable = true, concert = mockConcert(), price = 150.0)
        return Reservation(
            id = 1L,
            userId = UUID.randomUUID(),
            seat = seat,
            concert = mockConcert(),
            status = ReservationStatus.RESERVED,
            createdAt = createdAt
        )
    }

    private fun mockUser(userId: UUID) = User(id = userId, username = "testUser")

    private fun mockConcert() = Concert(id = 1L, name = "Concert 1", location = "lollapalooza", date = LocalDateTime.now(), availableTickets = 48)

    private fun mockQueue(userId: UUID) = Queue(
        userId = userId,
        status = QueueStatus.WAITING,
        concert = mockConcert(),
        token = UUID.randomUUID()
    )
}