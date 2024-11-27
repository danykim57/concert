package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.infrastructure.PointRepository
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository,

) {

    fun save(point: Point): Point {
        return pointRepository.save(point)
    }

    fun use(point: Point, reservation: Reservation) {
        // 포인트 잔액이 좌석 금액보다 적으면 예외 처리
        if (point.amount < reservation.seat.price) {
            throw IllegalArgumentException("포인트 잔액이 부족합니다.")
        }

        // 포인트 차감
        point.amount -= reservation.seat.price
        this.save(point)
    }
}