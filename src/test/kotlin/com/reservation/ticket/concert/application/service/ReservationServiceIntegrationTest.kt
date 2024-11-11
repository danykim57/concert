package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.*
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.infrastructure.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
class ReservationServiceIntegrationTest {

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var queueRepository: QueueRepository

    @Autowired
    private lateinit var queueRedisRepository: QueueRedisRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var pointRepository: PointRepository

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    private lateinit var testUser: User
    private lateinit var testConcert: Concert
    private lateinit var testSeat: Seat
    private lateinit var testReservation: Reservation
    private lateinit var testQueue: Queue
    private lateinit var testPoint: Point

    @BeforeEach
    fun setup() {
        // 유저 생성 및 저장
        testUser = User(
            id = UUID.randomUUID(),
            password = "password123"
        )
        userRepository.save(testUser)

        // 콘서트 생성 및 저장
        testConcert = Concert(
            id = 1L,
            name = "Test Concert",
            location = "Chicago",
            date = LocalDateTime.now(),
            availableTickets = 50,
        )
        concertRepository.save(testConcert)

        // 좌석 생성 및 저장
        testSeat = Seat(
            seatNumber = "A1",
            isAvailable = false,  // 예약된 상태로 설정
            concert = testConcert,
            price = 10.0,
        )
        seatRepository.save(testSeat)

        // 예약 생성 및 저장
        testReservation = Reservation(
            id = 1L,
            userId = testUser.id,
            seat = testSeat,
            concert = testConcert,
            status = ReservationStatus.RESERVED,
            createdAt = LocalDateTime.now().minusMinutes(6)  // 5분 경과
        )
        reservationRepository.save(testReservation)

        // 대기열 생성 및 저장
        testQueue = Queue(
            userId = testUser.id,
            status = QueueStatus.PASS,
            concert = testConcert,
            token = UUID.randomUUID()
        )
        queueRepository.save(testQueue)

        // 유저 포인트 생성 및 저장
        testPoint = Point(
            user = testUser,
            amount = 1000.0  // 충분한 포인트 설정
        )
        pointRepository.save(testPoint)
    }

    @Test
    fun `confirmReservation should complete payment and update reservation`() {
        // 예약 확인 처리
        val result = reservationService.confirmReservation(testReservation.id)

        // 결제 완료 메세지 확인
        assertEquals("예약이 결제 완료 처리되었습니다.", result)

        // 예약 상태가 결제 완료로 변경되었는지 확인
        val updatedReservation = reservationRepository.findById(testReservation.id).get()
        assertEquals(ReservationStatus.CONFIRMED, updatedReservation.status)

        // 대기열이 삭제되었는지 확인
        val queue = queueRepository.findByUserId(testUser.id)
        assertTrue(queue == null)

        // 좌석이 예약 가능한 상태로 변경되었는지 확인
        val updatedSeat = seatRepository.findById(testSeat.id).get()
        assertFalse(updatedSeat.isAvailable)

        // 포인트가 차감되었는지 확인
        val updatedPoint = pointRepository.findByUserId(testUser.id)
        assertNotNull(updatedPoint)
        assertEquals(1000 - testSeat.price, updatedPoint?.amount)

        // 결제 정보가 저장되었는지 확인
        val payments = paymentRepository.findAllByUserId(testUser.id)
        assertTrue(payments.isNotEmpty())
        assertEquals(testSeat.price, payments[0].amount)
    }

    @Test
    fun `confirmReservation should throw exception if reservation expired`() {
        // 예약 시간이 경과하여 결제가 불가능한 상태로 설정 (예약 취소 테스트)
        testReservation.createdAt = LocalDateTime.now().minusMinutes(10)  // 10분 경과
        reservationRepository.save(testReservation)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            reservationService.confirmReservation(testReservation.id)
        }

        // 예외 메시지 확인
        assertEquals("유효하지 않은 토큰 입니다.", exception.message)

        // 예약 상태가 취소로 변경되었는지 확인
        val updatedReservation = reservationRepository.findById(testReservation.id).get()
        assertEquals(ReservationStatus.CANCELLED, updatedReservation.status)

        // 좌석이 다시 예약 가능한 상태로 변경되었는지 확인
        val updatedSeat = seatRepository.findById(testSeat.id).get()
        assertTrue(updatedSeat.isAvailable)
    }
}
