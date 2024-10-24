package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Payment
import com.reservation.ticket.concert.domain.PaymentType
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.PaymentRepository
import com.reservation.ticket.concert.infrastructure.PointRepository
import com.reservation.ticket.concert.infrastructure.QueueRepository
import com.reservation.ticket.concert.infrastructure.ReservationRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import com.reservation.ticket.concert.infrastructure.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val seatRepository: SeatRepository,
    private val userRepository: UserRepository,
    private val concertRepository: ConcertRepository,
    private val pointRepository: PointRepository,
    private val paymentRepository: PaymentRepository,
    private val queueStatusChecker: QueueStatusChecker,
    private val queueRepository: QueueRepository,
) {

    fun get(id: Long): Reservation {
        return reservationRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("해당 예약이 존재하지 않습니다.")
        }
    }

    fun save(reservation: Reservation): Reservation {
        return reservationRepository.save(reservation)
    }

    // 결제 및 예약 상태 변경
    @Transactional
    fun confirmReservation(reservationId: Long): String {

        val reservation = reservationRepository.findById(reservationId).orElseThrow {
            throw IllegalArgumentException("해당 예약이 존재하지 않습니다.")
        }

        // 비관락이 들어감
        val user = userRepository.findById(reservation.userId).orElseThrow {
            throw IllegalArgumentException("해당 유저를 찾을 수 없습니다.")
        }

        if (queueStatusChecker.isQueueStatusPass(user.id)) {
            throw IllegalArgumentException("유효하지 않은 토큰 입니다.")
        }

        concertRepository.findById(reservation.concert.id).orElseThrow {
            throw IllegalArgumentException("콘서트가 존재하지 않습니다.")
        }

        val seat = seatRepository.findWriteLockById(reservation.seat.id).orElseThrow{
            throw IllegalArgumentException("존재하지 않은 좌석 입니다.")
        }

        val queue = queueRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("존재하지 않는 대기열 토큰 입니다.")

        if (reservation.createdAt?.plusMinutes(5)!!.isAfter(LocalDateTime.now())) {
            //대기열 삭제
            queueRepository.delete(queue)
            // 좌석 예약 가능으로 변경
            seat.isAvailable = true
            seatRepository.save(seat)
            // 예약 취소처리
            reservation.status = ReservationStatus.CANCELLED
            reservationRepository.save(reservation)
            throw IllegalArgumentException("")
        }


        // 상태를 결제 완료로 변경
        if (reservation.status != ReservationStatus.RESERVED) {
            throw IllegalArgumentException("결제 완료 처리를 할 수 없는 상태입니다.")
        }

        val point = pointRepository.findByUserId(user.id)
            ?: throw IllegalArgumentException("포인트가 설정되어 있지 않습니다.")
        // 포인트 잔액이 좌석 금액보다 적으면 예외 처리
        if (point.amount < reservation.seat.price) {
            throw IllegalArgumentException("포인트 잔액이 부족합니다.")
        }

        // 포인트 차감
        point.amount -= reservation.seat.price
        pointRepository.save(point)

        // 결제 정보 저장
        val payment = Payment(
            userId = reservation.userId,
            reservationId = reservation.id,
            amount = point.amount,
            type = PaymentType.SPEND
        )
        // 히스토리 저장
        paymentRepository.save(payment)

        // 예약 정보 저장
        reservation.status = ReservationStatus.CONFIRMED
        reservationRepository.save(reservation)

        //대기열 삭제
        queueRepository.delete(queue)

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