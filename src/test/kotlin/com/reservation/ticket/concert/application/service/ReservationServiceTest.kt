package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.application.token.QueueStatusChecker
import org.junit.jupiter.api.Assertions.*
import com.reservation.ticket.concert.domain.*
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.infrastructure.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
class ReservationServiceTest {

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var concertRepository: ConcertRepository

    @Mock
    private lateinit var seatRepository: SeatRepository

    @Mock
    private lateinit var queueRepository: QueueRepository

    @Mock
    private lateinit var pointRepository: PointRepository

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Mock
    private lateinit var queueStatusChecker: QueueStatusChecker

    @Mock
    private lateinit var queueService: QueueService

    @InjectMocks
    private lateinit var reservationService: ReservationService

    @BeforeEach
    fun setUp() {
        // Mockito의 Mock 객체들을 초기화
        MockitoAnnotations.openMocks(this)
    }
    @Test
    fun `should throw exception if reservation does not exist`() {
        // Given
        val reservationId = 1L
        `when`(reservationRepository.findById(reservationId)).thenReturn(Optional.empty())

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationService.confirmReservation(reservationId)
        }

        assertEquals("해당 예약이 존재하지 않습니다.", exception.message)
    }

    @Test
    fun `should throw exception if user does not exist`() {
        // Given
        val reservation = mockReservation()
        `when`(reservationRepository.findById(reservation.id)).thenReturn(Optional.of(reservation))
        `when`(userRepository.findById(reservation.userId)).thenReturn(Optional.empty())

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationService.confirmReservation(reservation.id)
        }

        assertEquals("해당 유저를 찾을 수 없습니다.", exception.message)
    }

    @Test
    fun `should cancel reservation if 5 minutes has passed`() {
        // Given
        val reservation = mockReservation(createdAt = LocalDateTime.now().minusMinutes(6))
        val seat = reservation.seat
        val queue = mockQueue(userId = reservation.userId)

        `when`(reservationRepository.findById(reservation.id)).thenReturn(Optional.of(reservation))
        `when`(userRepository.findById(reservation.userId)).thenReturn(Optional.of(mockUser(reservation.userId)))
        `when`(concertRepository.findById(reservation.concert.id)).thenReturn(Optional.of(reservation.concert))
        `when`(seatRepository.findById(reservation.seat.id)).thenReturn(Optional.of(seat))
        `when`(queueRepository.findByUserId(reservation.userId)).thenReturn(queue)

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationService.confirmReservation(reservation.id)
        }

        assertEquals("타임오버", exception.message)
        verify(queueRepository).delete(queue)
        assertTrue(seat.isAvailable)
        assertEquals(ReservationStatus.CANCELLED, reservation.status)
        verify(seatRepository).save(seat)
        verify(reservationRepository).save(reservation)
    }

    @Test
    fun `should throw exception if user has insufficient points`() {
        // Given
        val reservation = mockReservation()
        val user = mockUser(reservation.userId)
        val point = Point(user = user, amount = 50.0) // 부족한 포인트
        `when`(reservationRepository.findById(reservation.id)).thenReturn(Optional.of(reservation))
        `when`(userRepository.findById(reservation.userId)).thenReturn(Optional.of(user))
        `when`(pointRepository.findByUserId(reservation.userId)).thenReturn(point)

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            reservationService.confirmReservation(reservation.id)
        }

        assertEquals("포인트 잔액이 부족합니다.", exception.message)
    }

    @Test
    fun `should confirm reservation if user has sufficient points`() {
        // Given
        val reservation = mockReservation()
        val user = mockUser(reservation.userId)
        val point = Point(user = user, amount = 200.0) // 충분한 포인트
        `when`(reservationRepository.findById(reservation.id)).thenReturn(Optional.of(reservation))
        `when`(userRepository.findById(reservation.userId)).thenReturn(Optional.of(user))
        `when`(pointRepository.findByUserId(reservation.userId)).thenReturn(point)

        // When
        val result = reservationService.confirmReservation(reservation.id)

        // Then
        assertEquals("예약이 결제 완료 처리되었습니다.", result)
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
        assertEquals(50.0, point.amount) // 포인트가 차감된 상태
        verify(pointRepository).save(point)
        verify(paymentRepository).save(any(Payment::class.java))
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
