package com.reservation.ticket.concert.interfaces.api.seat

import com.reservation.ticket.concert.domain.dto.response.SeatResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController(value = "/api/seat")
class SeatController {

    @GetMapping("{concertId}")
    fun getAvailableSeats(@PathVariable concertId: String):SeatResponse  {
        return SeatResponse(
            code = "success",
            seats = listOf("1", "2", "3")
        )

    }
}