package com.reservation.ticket.concert.interfaces.api.user

import com.reservation.ticket.concert.application.service.QueueService
import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerConcurrencyTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var queueService: QueueService

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `multiple users send requests to get their queue position`() {

        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val tempConcert = Concert(name = "Concert 1", location = "lollapalooza", date = LocalDateTime.now(), availableTickets = 48)
        val users = (1..10).map {
            userService.saveUser(User(UUID.randomUUID(),"testUser$it", "password$it"))
        }

        val userIds = users.map { it -> it.id }
        val expectedPositions = (1..numberOfThreads).toList()

        val responses = mutableListOf<String>()

        for (i in 0 until numberOfThreads) {
            val userId = userIds[i]
            executorService.submit {
                try {
                    val response = mockMvc.get("/api/$userId/position")
                        .andExpect { status { isOk() } }
                        .andReturn().response.contentAsString

                    synchronized(responses) {
                        responses.add(response)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        assertThat(responses.size).isEqualTo(numberOfThreads)

        expectedPositions.forEach { position ->
            assertThat(responses).contains(position.toString())
        }
    }

    @Test
    fun `multiple users send their own point GET requests`() {

        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val userId = UUID.randomUUID()

        val responses = mutableListOf<String>()

        for (i in 0 until numberOfThreads) {
            executorService.submit {
                try {
                    // 포인트 조회 요청
                    val response = mockMvc.get("/api/user/$userId/point") {
                        accept(MediaType.APPLICATION_JSON)
                    }.andExpect {
                        status { isOk() }
                    }.andReturn().response.contentAsString

                    synchronized(responses) {
                        responses.add(response)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        assertThat(responses.size).isEqualTo(numberOfThreads)

        val firstResponse = responses.first()
        responses.forEach { response ->
            assertThat(response).isEqualTo(firstResponse)
        }
    }
    @Test
    fun `multiple users send 'add points' requests at once`() {

        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val userId = 1L

        val request = """
            {
                "amount": 100
            }
        """.trimIndent()

        val responses = mutableListOf<String>()

        for (i in 0 until numberOfThreads) {
            executorService.submit {
                try {
                    // 포인트 추가 요청
                    val response = mockMvc.put("/api/user/$userId/point/add") {
                        contentType = MediaType.APPLICATION_JSON
                        content = request
                    }.andExpect {
                        status { isOk() }
                    }.andReturn().response.contentAsString

                    synchronized(responses) {
                        responses.add(response)
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        assertThat(responses.size).isEqualTo(numberOfThreads)

        responses.forEach { response ->
            assertThat(response).contains("\"code\":\"OK\"")
            assertThat(response).contains("\"point\":100")
        }
    }

}