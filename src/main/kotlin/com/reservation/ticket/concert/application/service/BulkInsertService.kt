package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.PointRepository
import com.reservation.ticket.concert.infrastructure.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class BulkInsertService(
    private val concertRepository: ConcertRepository,
    private val userRepository: UserRepository,
    private val pointRepository: PointRepository
) {

    @Transactional
    fun bulkInsertConcerts(totalConcerts: Int, batchSize: Int = 1000) {
        val concerts = mutableListOf<Concert>()
        for (i in 1..totalConcerts) {
            concerts.add(
                Concert(
                    name = "Concert $i",
                    location = "Location $i",
                    date = LocalDateTime.now().plusDays(i.toLong()),
                    availableTickets = Random.nextInt(10, 100)
                )
            )
            if (i % batchSize == 0) {
                concertRepository.saveAll(concerts)
                concerts.clear()
                println("Inserted $i concerts")
            }
        }
        if (concerts.isNotEmpty()) {
            concertRepository.saveAll(concerts)
            println("Inserted remaining concerts")
        }
    }

    @Transactional
    fun bulkInsertUsers(totalUsers: Int, batchSize: Int = 1000) {
        val users = mutableListOf<User>()
        for (i in 1..totalUsers) {
            users.add(User(username = "User $i"))
            if (i % batchSize == 0) {
                userRepository.saveAll(users)
                users.clear()
                println("Inserted $i users")
            }
        }
        if (users.isNotEmpty()) {
            userRepository.saveAll(users)
            println("Inserted remaining users")
        }
    }

    @Transactional
    fun bulkInsertPoints(totalPoints: Int, batchSize: Int = 1000) {
        val users = userRepository.findAll() // 모든 유저를 가져와서 랜덤으로 할당
        val points = mutableListOf<Point>()
        for (i in 1..totalPoints) {
            points.add(
                Point(
                    user = users.random(), // 임의의 유저에게 포인트 할당
                    amount = Random.nextDouble(500.0, 2000.0)
                )
            )
            if (i % batchSize == 0) {
                pointRepository.saveAll(points)
                points.clear()
                println("Inserted $i points")
            }
        }
        if (points.isNotEmpty()) {
            pointRepository.saveAll(points)
            println("Inserted remaining points")
        }
    }
}