package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.infrastructure.ReservationRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val seatRepository: SeatRepository  // 좌석 정보를 가져오기 위한 Repository
) {

    fun save(reservation: Reservation): Reservation {
        return reservationRepository.save(reservation)
    }

    // 예약 상태 변경 (예: 결제 완료 처리)
    @Transactional
    fun confirmReservation(reservationId: Long): String {
        // 예약 정보 가져오기
        val reservation = reservationRepository.findById(reservationId).orElseThrow {
            throw IllegalArgumentException("해당 예약이 존재하지 않습니다.")
        }

        // 상태를 결제 완료로 변경
        if (reservation.status != ReservationStatus.RESERVED) {
            throw IllegalArgumentException("결제 완료 처리를 할 수 없는 상태입니다.")
        }
        reservation.status = ReservationStatus.CONFIRMED
        reservationRepository.save(reservation)

        return "예약이 결제 완료 처리되었습니다."
    }

    // 예약 취소
    @Transactional
    fun cancelReservation(reservationId: Long): String {
        // 예약 정보 가져오기
        val reservation = reservationRepository.findById(reservationId).orElseThrow {
            throw IllegalArgumentException("해당 예약이 존재하지 않습니다.")
        }

        // 예약 상태 변경
        if (reservation.status != ReservationStatus.RESERVED) {
            throw IllegalArgumentException("취소할 수 없는 예약 상태입니다.")
        }
        reservation.status = ReservationStatus.CANCELLED
        reservationRepository.save(reservation)

        // 좌석을 다시 사용 가능하도록 업데이트
        val seat = reservation.seat
        seat.isAvailable = true
        seatRepository.save(seat)

        return "예약이 취소되었습니다."
    }

    // 특정 유저의 예약 내역 확인
    fun getUserReservations(userId: UUID): List<Reservation> {
        return reservationRepository.findAllByUserId(userId)
    }
}