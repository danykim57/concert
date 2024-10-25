package com.reservation.ticket.concert.interfaces.api.token

import com.reservation.ticket.concert.application.service.ConcertService
import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.interfaces.request.TokenRequest
import com.reservation.ticket.concert.interfaces.response.TokenResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExtendWith(SpringExtension::class)
@SpringBootTest
class TokenConcurrencyTest {

    @Autowired
    private lateinit var tokenController: TokenController

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var concertService: ConcertService

    @Autowired
    private lateinit var queueService: QueueService

    private lateinit var user: User
    private lateinit var concert: Concert
    private lateinit var users: List<User>

    @BeforeEach
    fun setup() {
        // 테스트에 사용할 유저와 콘서트 정보 셋업
        val tempConcert = Concert(name = "Concert 1", location = "lollapalooza", date = LocalDateTime.now(), availableTickets = 48)
        users = (1..10).map {
            userService.saveUser(User(UUID.randomUUID(),"testUser$it", "password$it"))
        }
        concert = concertService.create(tempConcert)
    }

    @Test
    fun `execute multiple token generations at once and ensure all users get tokens`() {

        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val responses = mutableListOf<TokenResponse>()

        for (i in 0 until numberOfThreads) {
            val user = users[i]
            executorService.submit {
                try {
                    val req = TokenRequest(
                        id = user.username,
                        password = "password${i + 1}",
                        concertCode = concert.id.toString()
                    )

                    val response = tokenController.getToken(req)

                    synchronized(responses) {
                        responses.add(response)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        val successResponses = responses.filter { it.status == HttpStatus.OK.value() }

        val distinctTokens = successResponses.map { it.token }.distinct()

        // Assertions
        assertThat(responses.size).isEqualTo(numberOfThreads)
        assertThat(successResponses.size).isEqualTo(numberOfThreads)
        assertThat(distinctTokens.size).isEqualTo(numberOfThreads)
    }

}