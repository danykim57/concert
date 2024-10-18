package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.ReservationService
import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.domain.dto.SeatDTO
import com.reservation.ticket.concert.domain.toDto
import com.reservation.ticket.concert.infrastructure.ReservationRepository
import com.reservation.ticket.concert.interfaces.request.BookingRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class BookingFacade(
    private val seatService: SeatService,
    private val queueService: QueueService,
    private val queueStatusChecker: QueueStatusChecker,
    private val concertService: ConcertService,
    private val reservationService: ReservationService
) {

    @Transactional
    fun book(request: BookingRequest, userId: UUID): SeatDTO {
        if (queueStatusChecker.isQueueStatusPass(userId) == false)
            throw IllegalArgumentException("아직 대기중입니다.")

        if (!queueService.isValidToken(request.queueToken)) {
            throw IllegalArgumentException("유효하지 않은 대기열 토큰입니다.")
        }

        val concert = concertService.get(request.concertId)
        if (concert.isEmpty)
            throw IllegalArgumentException("없는 콘서트 입니다.")

        val seat = seatService.get(request.seatId)
            ?: throw IllegalArgumentException("해당 좌석이 존재하지 않습니다.")


        if (!seat.isAvailable) {
            throw IllegalStateException("해당 좌석은 이미 예약되었습니다.")
        }

        seat.isAvailable = false
        seatService.save(seat)

        val reservation = Reservation(
            userId = userId,
            seat = seat,
            concert = concert.get(),
            status = ReservationStatus.RESERVED // 예약 상태로 저장
        )
        reservationService.save(reservation)

        return seat.toDto()
    }

}