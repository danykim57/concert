package com.reservation.ticket.concert.interfaces.api.user

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.interfaces.request.PointRequest
import com.reservation.ticket.concert.interfaces.response.PointResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController(value = "api")
class UserController(
    private val userService: UserService,
    private val queueService: QueueService,
) {

    @GetMapping("user/{userId}/point")
    fun getMyPoint(@PathVariable userId: UUID): PointResponse {
        val point = userService.get(userId)
        return PointResponse(
            code = "success",
            point = point
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

    @PutMapping("user/{userId}/point/add")
    fun addPoint(@PathVariable userId: Long, @RequestBody req: PointRequest): ResponseEntity<PointResponse> {
        val point = userService.add(req)
        return ResponseEntity.ok(
            PointResponse(
                code = "success",
                point = point.amount,
            )
        )
    }

    @PutMapping("user/{userId}/point/spend")
    fun spendPoint(@PathVariable userId: Long, @RequestBody req: PointRequest): ResponseEntity<PointResponse> {
        val point = userService.spend(req)
        return ResponseEntity.ok(
            PointResponse(
                code = "success",
                point = point.amount,
            )
        )
    }

    // 유저의 대기 순번을 조회하는 API
    @GetMapping("/{userId}/position")
    fun getUserQueuePosition(@PathVariable userId: UUID): Int {
        return queueService.getUserQueuePosition(userId)
    }
}