package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.ReservationService
import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.application.token.QueueStatusChecker
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.domain.dto.SeatDTO
import com.reservation.ticket.concert.domain.toDto
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import com.reservation.ticket.concert.interfaces.request.BookingRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class BookingFacade(
    private val seatService: SeatService,
    private val concertService: ConcertService,
    private val reservationService: ReservationService
) {

    @Transactional
    fun book(request: BookingRequest, userId: UUID): SeatDTO {

        val concert = concertService.get(request.concertId)
        if (concert.isEmpty)
            throw UnprocessableEntityException("없는 콘서트 입니다.")

        val seat = seatService.save(request.seatId)

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