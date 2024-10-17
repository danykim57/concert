package com.reservation.ticket.concert.interfaces.api.seat

import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.domain.toDto
import com.reservation.ticket.concert.interfaces.response.SeatResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController(value = "/api/seat")
class SeatController(
    private val seatService: SeatService
) {
    @Operation(summary = "예약가능 좌석 조회", description = "예약가능 좌석 조회 API")
    @GetMapping("{date}")
    fun getAvailableSeats(@PathVariable concertId: String, @PathVariable date: LocalDateTime): SeatResponse {
        val seats =  seatService.getAvailableSeats(date).map { it.toDto() }
        return SeatResponse(
            code = "success",
            seats = seats
        )
    }
}