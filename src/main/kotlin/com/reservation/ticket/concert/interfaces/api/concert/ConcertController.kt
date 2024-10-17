package com.reservation.ticket.concert.interfaces.api.concert

import com.reservation.ticket.concert.application.facade.BookingFacade
import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.domain.dto.ConcertDTO
import com.reservation.ticket.concert.interfaces.request.BookingRequest
import com.reservation.ticket.concert.interfaces.response.ReservationResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController(value = "api/concert")
class ConcertController(
    private val concertService: ConcertService,
    private val bookingService: BookingFacade,
    private val queueService: QueueService,
) {

    @Operation(summary = "예약 가능 날짜 조회", description = "예약 가능 날짜 조회 API")
    @GetMapping("/concerts/available")
    fun getAvailableConcerts(): List<ConcertDTO> {
        return concertService.getAvailableConcerts().map { concert ->
            ConcertDTO(
                id = concert.id,
                name = concert.name,
                location = concert.location,
                date = concert.date,
                availableTickets = concert.availableTickets
            )
        }
    }

    @Operation(summary = "좌석 예약 요청", description = "좌석 예약 요청 API")
    @PostMapping("/book")
    fun bookSeat(@RequestBody request: BookingRequest, @RequestHeader token: UUID): ReservationResponse {
        val seat = bookingService.book(request, token)
        return ReservationResponse(
            code = "success",
            seat = seat
        )
    }

}