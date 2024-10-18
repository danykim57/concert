package com.reservation.ticket.concert.interfaces.api.user

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.interfaces.request.PointRequest
import com.reservation.ticket.concert.interfaces.response.PointResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController(value = "api")
class UserController(
    private val userService: UserService,
    private val queueService: QueueService,
) {

    @GetMapping("user/{userId}/point")
    fun getMyPoint(@PathVariable userId: Long): PointResponse {
        return PointResponse(
            code = "success",
            point = 123.0
        )
    }

    @GetMapping("/users/{userId}/points")
    fun getUserPoints(@PathVariable userId: UUID): PointResponse {
        val point = userService.get(userId)
        return PointResponse(
            code = "success",
            point = point
        )
    }

    @PutMapping("user/{userId}/point")
    fun addPoint(@PathVariable userId: Long, @RequestBody req: PointRequest): ResponseEntity<PointResponse> {
        userService.add(req)
        return ResponseEntity.ok(
            PointResponse(
            code = "success",
            point = 123.0,
        )
        )
    }

    // 유저의 대기 순번을 조회하는 API
    @GetMapping("/{userId}/position")
    fun getUserQueuePosition(@PathVariable userId: UUID): Int {
        return queueService.getUserQueuePosition(userId)
    }
}