package com.reservation.ticket.concert.interfaces.api.concert

import com.reservation.ticket.concert.domain.dto.ConcertDto
import com.reservation.ticket.concert.domain.dto.response.CommonResponse
import com.reservation.ticket.concert.domain.dto.response.ConcertResponse
import com.reservation.ticket.concert.domain.dto.response.ReservationResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController(value = "api/concert")
class ConcertController {

    @GetMapping
    fun getAvailableConcerts(): ConcertResponse {
        return ConcertResponse(
            code = "success",
            concerts = listOf(
                ConcertDto("1", LocalDateTime.now().toString()),
                ConcertDto("2", LocalDateTime.now().toString()),
                ConcertDto("3", LocalDateTime.now().toString())
            )
        )
    }

    @PostMapping
    fun makeConcertReservation(): ReservationResponse {
        return ReservationResponse(
            code = "success",
            concert = ConcertDto("1", LocalDateTime.now().toString()),
        )

    }
}