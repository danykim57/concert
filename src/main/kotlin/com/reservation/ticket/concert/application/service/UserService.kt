package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.domain.PointAction
import com.reservation.ticket.concert.domain.PointHistory
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.PointHistoryRepository
import com.reservation.ticket.concert.infrastructure.PointRepository
import com.reservation.ticket.concert.infrastructure.UserRepository
import com.reservation.ticket.concert.interfaces.request.PointRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository
) {
    fun get(username: String, password: String): User {
        val user = userRepository.findByUsernameAndPassword(username, password)
            ?:throw IllegalArgumentException("해당 ID 또는 패스워드가 잘못되었습니다.")

        return user
    }

    fun getUser(userId: UUID): User {
        return userRepository.findById(userId).orElseThrow {
            throw IllegalArgumentException("해당 유저를 찾을 수 없습니다.")
        }
    }

    fun saveUser(user: User): User {
        return userRepository.save(user)
    }

    fun getUserWithLock(userId: UUID): User {
         val user = userRepository.findWriteLockById(userId)
            ?: throw IllegalArgumentException("해당 유저를 찾을 수 없습니다.")
         return user
    }

    fun getPoint(userId: UUID): Point {
        return pointRepository.findByUserId(userId) ?: throw IllegalArgumentException("해당 유저의 포인트 정보가 존재하지 않습니다.")
    }

    // 유저의 포인트를 조회하는 메서드
    fun get(userId: UUID): Double {
        val point = pointRepository.findByUserId(userId) ?: throw IllegalArgumentException("해당 유저의 포인트 정보가 존재하지 않습니다.")
        return point.amount
    }

    @Transactional
    fun add(request: PointRequest): Point {
        // 유저 ID로 포인트 정보 조회
        var point = pointRepository.findByUserId(request.userId)
        val user = userRepository.findById(request.userId).get()
        if (point == null) {
            // 포인트 정보가 없는 경우 새로운 포인트 엔티티 생성
            point = Point(amount = request.amount, user = user) // 유저 엔티티가 필요
        } else {
            point.amount += request.amount
        }
        val result = pointRepository.save(point)
        pointHistoryRepository.save(PointHistory(user = user, amount = request.amount, action = PointAction.CHARGE))
        return result
    }

    @Transactional
    fun spend(request: PointRequest): Point {
        var point = pointRepository.findByUserId(request.userId)
        val user = userRepository.findById(request.userId).get()
        if (point == null) {
            // 포인트 정보가 없는 경우 새로운 포인트 엔티티 생성
            point = Point(amount = request.amount, user = User(id = request.userId)) // 유저 엔티티가 필요
        } else {
            point.amount -= request.amount
            if (point.amount < 0) { throw IllegalArgumentException("포인트가 부족합니다.")}
        }
        val result = pointRepository.save(point)
        pointHistoryRepository.save(PointHistory(user = user, amount = request.amount, action = PointAction.SPEND))
        return result
    }

    fun getUserHistory(userId: UUID): List<PointHistory> {
        return pointHistoryRepository.findAllByUserId(userId)
    }

}