package com.reservation.ticket.concert.application.facade

import com.fasterxml.jackson.databind.ObjectMapper
import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.OutboxService
import com.reservation.ticket.concert.application.service.PaymentService
import com.reservation.ticket.concert.application.service.PointService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.ReservationService
import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.Queue
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationMessage
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.infrastructure.event.EventPublisher
import com.reservation.ticket.concert.infrastructure.event.OutboxEvent
import com.reservation.ticket.concert.infrastructure.event.OutboxProducer
import com.reservation.ticket.concert.infrastructure.event.ReservationEvent
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
    private val outboxProducer: OutboxProducer,
    ){
    // 결제 및 예약 상태 변경
    @Transactional
    fun confirmReservation(reservationId: Long): String {

        val reservation = reservationService.get(reservationId)
        val queue = queueService.get(reservation.userId)
        val user = userService.getUserWithLock(reservation.userId) // 비관락이 들어감
        val point = userService.getPoint(user.id)

        validateReservation(reservation, queue)

        pointService.use(point, reservation)

        paymentService.save(point, reservation)

        reservation.status = ReservationStatus.CONFIRMED
        val objectMapper = ObjectMapper()
        outboxProducer.publish(OutboxEvent(reservation, objectMapper = objectMapper))

        //대기열 삭제
        queueService.delete(queue)

        return ReservationMessage.CONFIRM.message
    }

    fun validateReservation(reservation: Reservation, queue: Queue) {
        concertService.get(reservation.concert.id)
        val seat = seatService.get(reservation.seat.id)!!

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

    }
}