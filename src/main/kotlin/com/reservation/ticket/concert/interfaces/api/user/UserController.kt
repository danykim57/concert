package com.reservation.ticket.concert.interfaces.api.user

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.interfaces.request.PointRequest
import com.reservation.ticket.concert.interfaces.response.CommonResponse
import com.reservation.ticket.concert.interfaces.response.PointResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService,
    private val queueService: QueueService,
) {

    @GetMapping("user/{userId}/point")
    fun getMyPoint(@PathVariable userId: UUID): PointResponse {
        val point = userService.get(userId)
        return PointResponse(
            status = HttpStatus.OK.value(),
            code = HttpStatus.OK.reasonPhrase,
            point = point
        )
    }

    @GetMapping("/users/{userId}/points")
    fun getUserPoints(@PathVariable userId: UUID): PointResponse {
        val point = userService.get(userId)
        return PointResponse(
            status = HttpStatus.OK.value(),
            code = HttpStatus.OK.reasonPhrase,
            point = point
        )
    }

    @PutMapping("user/point/add")
    fun addPoint(@RequestBody req: PointRequest): ResponseEntity<PointResponse> {
        val point = userService.add(req)
        return ResponseEntity.ok(
            PointResponse(
                status = HttpStatus.OK.value(),
                code = HttpStatus.OK.reasonPhrase,
                point = point.amount,
            )
        )
    }

    @PutMapping("user/point/spend")
    fun spendPoint(@RequestBody req: PointRequest): ResponseEntity<PointResponse> {
        val point = userService.spend(req)
        return ResponseEntity.ok(
            PointResponse(
                status = HttpStatus.OK.value(),
                code = HttpStatus.OK.reasonPhrase,
                point = point.amount,
            )
        )
    }

    // 유저의 대기 순번을 조회하는 API
    @GetMapping("/{userId}/position")
    fun getUserQueuePosition(@PathVariable userId: UUID): Int {
        return queueService.getUserQueuePosition(userId)
    }

    @GetMapping("/test/user")
    fun addUser(): CommonResponse {
        val user = User(
            id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
        )
        userService.saveUser(user);
        return CommonResponse(
            status = HttpStatus.OK.value(),
            code = HttpStatus.OK.reasonPhrase,
            message = "success"
        )
    }
}