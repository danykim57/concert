package com.reservation.ticket.concert.interfaces.api.point

import com.reservation.ticket.concert.domain.dto.request.PointRequst
import com.reservation.ticket.concert.domain.dto.response.PointResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController(value = "api")
class PointController {

    @GetMapping("user/{userId}/point")
    fun getMyPoint(@PathVariable userId: Long): PointResponse {
        return PointResponse(
            code = "success",
            point = 123.0
        )
    }

    @PutMapping("user/{userId}/point")
    fun addPoint(@PathVariable userId: Long, @RequestBody req: PointRequst): ResponseEntity<PointResponse> {
        return ResponseEntity.ok(PointResponse(
            code = "success",
            point = 123.0,
        ))
    }
}