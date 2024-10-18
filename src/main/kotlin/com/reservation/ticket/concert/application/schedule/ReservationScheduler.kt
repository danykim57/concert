package com.reservation.ticket.concert.application.schedule

import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.ReservationRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservationScheduler(
    private val reservationRepository: ReservationRepository,
    private val queueRepository: QueueRepository,
    private val seatRepository: SeatRepository
) {

    @Scheduled(fixedRate = 60000)  // 1분마다 실행
    @Transactional
    fun cancelUnpaidReservations() {
        // 현재 시간
        val now = LocalDateTime.now()

        // 예약 시간이 5분이 경과했고, 결제 상태가 완료되지 않은 예약 조회
        val unpaidReservations = reservationRepository.findUnpaidReservations(now.minusMinutes(5))

        unpaidReservations.forEach { reservation ->
            // 1. 대기열 삭제
            val queue = queueRepository.findByUserId(reservation.userId)
            queue?.let {
                queueRepository.delete(it)
            }

            // 2. 좌석 점유 해제
            val seat = reservation.seat
            seat.isAvailable = true
            seatRepository.save(seat)

            // 3. 예약 정보 업데이트 -> 예약 취소 처리
            reservation.status = ReservationStatus.CANCELLED
            reservationRepository.save(reservation)

            println("Reservation for seat ${seat.seatNumber} has been cancelled due to non-payment.")
        }
    }
}
