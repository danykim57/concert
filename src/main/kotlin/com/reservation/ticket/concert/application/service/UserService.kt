package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.PointRepository
import com.reservation.ticket.concert.infrastructure.UserRepository
import com.reservation.ticket.concert.interfaces.request.PointRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val pointRepository: PointRepository,
) {
    fun get(username: String, password: String): User {
        val user = userRepository.findByUsernameAndPassword(username, password)
            ?:throw IllegalArgumentException("해당 ID 또는 패스워드가 잘못되었습니다.")

        return user
    }

    // 유저의 포인트를 조회하는 메서드
    fun get(userId: UUID): Double {
        val point = pointRepository.findByUserId(userId) ?: throw IllegalArgumentException("해당 유저의 포인트 정보가 존재하지 않습니다.")
        return point.amount
    }

    @Transactional
    fun add(request: PointRequest) {
        // 유저 ID로 포인트 정보 조회
        val point = pointRepository.findByUserId(request.userId)

        if (point == null) {
            // 포인트 정보가 없는 경우 새로운 포인트 엔티티 생성
            val newPoint = Point(amount = request.amount, user = User(id = request.userId)) // 유저 엔티티가 필요
            pointRepository.save(newPoint)
        } else {
            point.amount += request.amount
            pointRepository.save(point)
        }
    }



}