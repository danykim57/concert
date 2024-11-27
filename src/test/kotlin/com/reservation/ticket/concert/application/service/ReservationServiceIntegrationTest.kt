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
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var queueRepository: QueueRepository

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var concertRepository: ConcertRepository

    @Autowired
    private lateinit var pointRepository: PointRepository

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



}
