package com.reservation.ticket.concert.application.schedule

import org.junit.jupiter.api.Assertions.*
import com.reservation.ticket.concert.domain.*
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.ReservationRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import com.reservation.ticket.concert.interfaces.schedule.ReservationScheduler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.anyOrNull
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ReservationSchedulerTest {

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var queueRepository: QueueRepository

    @Mock
    private lateinit var seatRepository: SeatRepository

    @InjectMocks
    private lateinit var reservationScheduler: ReservationScheduler

    private lateinit var testReservation: Reservation
    private lateinit var testSeat: Seat
    private lateinit var testQueue: Queue

    @BeforeEach
    fun setup() {
        // 테스트용 Seat, Reservation, Queue 객체 생성
        testSeat = Seat(
            id = 1L,
            seatNumber = "A1",
            isAvailable = false,
            concert = mock(Concert::class.java),
            price = 10.0
        )

        testReservation = Reservation(
            id = 1L,
            userId = UUID.randomUUID(),
            seat = testSeat,
            concert = mock(Concert::class.java),  // 임의의 Concert 객체
            status = ReservationStatus.RESERVED,
            createdAt = LocalDateTime.now().minusMinutes(6),  // 5분 경과
            updatedAt = LocalDateTime.now()
        )

        testQueue = Queue(
            id = 1L,
            userId = testReservation.userId,
            status = QueueStatus.PASS,
            token = UUID.randomUUID(),
            concert = mock(Concert::class.java),
        )
    }

    @Test
    fun `should cancel unpaid reservations after 5 minutes`() {
        // Mock 설정: 5분이 경과한 unpaid reservations 리턴
        `when`(reservationRepository.findUnpaidReservations(anyOrNull()))
            .thenReturn(listOf(testReservation))

        // Mock 설정: 유저의 대기열 정보
        `when`(queueRepository.findByUserId(testReservation.userId)).thenReturn(testQueue)

        // 스케줄러 실행
        reservationScheduler.cancelUnpaidReservations()

        // 예약 취소, 대기열 삭제, 좌석 상태 업데이트가 호출되었는지 확인
        verify(queueRepository).delete(testQueue)
        verify(seatRepository).save(testSeat)
        verify(reservationRepository).save(testReservation)

        // 예약 상태가 CANCELLED로 변경되었는지 확인
        assert(testReservation.status == ReservationStatus.CANCELLED)
        // 좌석이 다시 예약 가능 상태로 변경되었는지 확인
        assert(testSeat.isAvailable)
    }

    @Test
    fun `should not cancel reservations if no unpaid reservations exist`() {
        // Mock 설정: unpaid reservations 없음
        `when`(reservationRepository.findUnpaidReservations(anyOrNull()))
            .thenReturn(emptyList())

        // 스케줄러 실행
        reservationScheduler.cancelUnpaidReservations()

        // 아무런 동작도 일어나지 않는지 확인
        verify(queueRepository, never()).delete(anyOrNull())
        verify(seatRepository, never()).save(anyOrNull())
        verify(reservationRepository, never()).save(anyOrNull())
    }
}
