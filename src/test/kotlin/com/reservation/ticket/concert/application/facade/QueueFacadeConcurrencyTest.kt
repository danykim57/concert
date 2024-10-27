package com.reservation.ticket.concert.application.facade

import com.reservation.ticket.concert.application.service.*
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.interfaces.request.TokenRequest
import com.reservation.ticket.concert.interfaces.response.TokenResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExtendWith(SpringExtension::class)
@SpringBootTest
class QueueFacadeConcurrencyTest {

    @Autowired
    private lateinit var queueFacade: QueueFacade

    @Autowired
    private lateinit var queueService: QueueService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var concertService: ConcertService

    @Autowired
    private lateinit var seatService: SeatService

    private val tokenRequests: MutableList<TokenRequest> = mutableListOf()

    private val users: MutableList<User> = mutableListOf()

    private val seats: MutableList<Seat> = mutableListOf()

    private val concert: Concert = Concert(name = "Book of Mormon", location = "Broadway", date = LocalDateTime.now(), availableTickets = 50)


    @BeforeEach
    fun setup() {
        // Create test users and concert for the requests
        for (i in 1..5) {
            val user = userService.saveUser(User())
            users.add(user)
            val seat =seatService.save(Seat(
                seatNumber = "A$i",
                concert = concert,
                price = 300.0
            ))
            tokenRequests.add(TokenRequest(id = user.id.toString(), password = user.password, concertCode = concert.id.toString()))
        }
    }

    @Test
    @Transactional
    fun `concurrency test - users registering for queue in order`() {
        // Create a thread pool to run 5 threads (1 per user)
        val userCount = 5
        val executorService: ExecutorService = Executors.newFixedThreadPool(userCount)
        val latch = CountDownLatch(userCount)

        // Store the token responses to check order later
        val tokenResponses: MutableList<TokenResponse> = mutableListOf()

        // Register each user in the queue concurrently
        for (i in 0 until userCount) {
            executorService.submit {
                try {
                    val response = queueFacade.getToken(tokenRequests[i])
                    synchronized(tokenResponses) {
                        tokenResponses.add(response)
                    }
                } catch (e: Exception) {
                    println("Exception: ${e.message}")
                } finally {
                    latch.countDown() // Decrease latch count when a thread completes
                }
            }
        }

        // Wait until all threads have finished
        latch.await()

        // Check that all responses have been recorded
        assertEquals(userCount, tokenResponses.size)

        // Verify that each token was registered in the correct order
        for (i in 0 until userCount) {
            val tokenResponse = tokenResponses[i]
            assertEquals(HttpStatus.OK.value(), tokenResponse.status)
            assertEquals(HttpStatus.OK.reasonPhrase, tokenResponse.code)
            // Check the queue order based on token (you need to implement a queueService check for order)
            val positionInQueue = queueService.getUserQueuePosition(users[i].id)
            assertEquals(i + 1, positionInQueue)  // Ensuring the order is correct
        }
    }
}
