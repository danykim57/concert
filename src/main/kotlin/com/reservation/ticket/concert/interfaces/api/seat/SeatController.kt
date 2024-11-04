package com.reservation.ticket.concert.interfaces.api.seat

import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.domain.toDto
import com.reservation.ticket.concert.interfaces.request.SeatRequest
import com.reservation.ticket.concert.interfaces.response.SeatResponse
import com.reservation.ticket.concert.interfaces.response.SingleSeatResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/seat")
class SeatController(
    private val seatService: SeatService
) {
    @Operation(summary = "예약가능 좌석 조회", description = "예약가능 좌석 조회 API")
    @GetMapping("{date}")
    fun getAvailableSeats(@PathVariable concertId: String, @PathVariable date: LocalDateTime): SeatResponse {
        val seats =  seatService.getAvailableSeats(date).map { it.toDto() }
        return SeatResponse(
            status = HttpStatus.OK.value(),
            code = HttpStatus.OK.reasonPhrase,
            seats = seats
        )
    }

    @GetMapping("/saveForTest")
    fun saveForTest(): SingleSeatResponse {
        val seat = seatService.createSeatForTest()
        return SingleSeatResponse(
            status = HttpStatus.OK.value(),
            code = HttpStatus.OK.reasonPhrase,
            seat = seat.toDto()
        )
    }

    @PostMapping("/reserve")
    fun reserve(@RequestBody seatRequestBody: SeatRequest): SingleSeatResponse {
        val seat =  seatService.save(seatRequestBody.seatId)
        return SingleSeatResponse(
            status = HttpStatus.OK.value(),
            code = HttpStatus.OK.reasonPhrase,
            seat = seat.toDto()
        )

    }
}