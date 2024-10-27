package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.infrastructure.PointRepository
import org.springframework.stereotype.Service

@Service
class PointService(
    private val pointRepository: PointRepository,

) {

    fun save(point: Point): Point {
        return pointRepository.save(point)
    }
}