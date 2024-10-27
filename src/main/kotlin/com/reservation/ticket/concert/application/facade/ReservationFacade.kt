package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.PaymentService
import com.reservation.ticket.concert.application.service.PointService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.ReservationService
import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Payment
import com.reservation.ticket.concert.domain.PaymentType
import com.reservation.ticket.concert.domain.ReservationMessage
import com.reservation.ticket.concert.domain.ReservationStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservationFacade (
    private val reservationService: ReservationService,
    private val userService: UserService,
    private val concertService: ConcertService,
    private val seatService: SeatService,
    private val queueService: QueueService,
    private val pointService: PointService,
    private val paymentService: PaymentService,
    ){

    // 결제 및 예약 상태 변경
    @Transactional
    fun confirmReservation(reservationId: Long): String {

        val reservation = reservationService.get(reservationId)

        // 비관락이 들어감
        val user = userService.getUserWithLock(reservation.userId)

        concertService.get(reservation.concert.id)

        val seat = seatService.get(reservation.seat.id)!!

        val queue = queueService.get(reservation.userId)

        if (reservation.createdAt?.plusMinutes(5)!!.isBefore(LocalDateTime.now())) {
            //대기열 삭제
            queueService.delete(queue)
            // 좌석 예약 가능으로 변경
            seat.isAvailable = true
            seatService.save(seat)
            // 예약 취소처리
            reservation.status = ReservationStatus.CANCELLED
            reservationService.save(reservation)
            throw IllegalArgumentException("만료된 대기열 토큰입니다.")
        }


        // 상태를 결제 완료로 변경
        if (reservation.status != ReservationStatus.RESERVED) {
            throw IllegalArgumentException("결제 완료 처리를 할 수 없는 상태입니다.")
        }

        val point = userService.getPoint(user.id)

        // 포인트 잔액이 좌석 금액보다 적으면 예외 처리
        if (point.amount < reservation.seat.price) {
            throw IllegalArgumentException("포인트 잔액이 부족합니다.")
        }

        // 포인트 차감
        point.amount -= reservation.seat.price
        pointService.save(point)

        // 결제 정보 저장
        val payment = Payment(
            userId = reservation.userId,
            reservationId = reservation.id,
            amount = point.amount,
            type = PaymentType.SPEND
        )
        // 히스토리 저장
        paymentService.save(payment)

        // 예약 정보 저장
        reservation.status = ReservationStatus.CONFIRMED
        reservationService.save(reservation)

        //대기열 삭제
        queueService.delete(queue)

        return ReservationMessage.CONFIRM.message
    }
}