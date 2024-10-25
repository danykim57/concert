package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.*
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Reservation
import com.reservation.ticket.concert.domain.ReservationStatus
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.domain.User
import org.apache.commons.lang3.mutable.Mutable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExtendWith(SpringExtension::class)
@SpringBootTest
class ReservationFacadeConcurrencyTest {

    @Autowired
    private lateinit var reservationFacade: ReservationFacade

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var seatService: SeatService

    private val reservationIds: MutableList<Long> = mutableListOf()

    private val users: MutableList<User> = mutableListOf()

    private val seats: MutableList<Seat> = mutableListOf()

    private val concert: Concert = Concert(name = "Book of Mormon", location = "Broadway", date = LocalDateTime.now(), availableTickets = 50)

    @BeforeEach
    fun setup() {
        for (i in 1..5) {
            val user = userService.saveUser(User())
            users.add(user)
            val seat =seatService.save(Seat(
                seatNumber = "A$i",
                concert = concert,
                price = 300.0
            ))
            val reservation = reservationService.save(Reservation(seat=seat, concert = concert, userId = user.id ))
            reservationIds.add(reservation.id)
        }
    }

    @Test
    @Transactional
    fun `concurrency test - multiple users confirm reservation simultaneously`() {

        val userCount = 5
        val executorService: ExecutorService = Executors.newFixedThreadPool(userCount)
        val latch = CountDownLatch(userCount)

        for (i in 0 until userCount) {
            executorService.submit {
                try {
                    reservationFacade.confirmReservation(reservationIds[i])
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        reservationIds.forEach { reservationId ->
            val reservation = reservationService.get(reservationId)
            assertEquals(ReservationStatus.CONFIRMED, reservation.status)
        }
    }
}
