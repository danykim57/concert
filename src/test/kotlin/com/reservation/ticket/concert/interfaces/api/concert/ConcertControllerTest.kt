package com.reservation.ticket.concert.interfaces.api.concert

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class ConcertControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `multiple users send requests to get available concerts and all gets the result`() {
        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val responses = mutableListOf<String>()

        for (i in 0 until numberOfThreads) {
            executorService.submit {
                try {
                    // 예약 가능 날짜 조회 요청
                    val response = mockMvc.get("/api/concert/concerts/available")
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

        val firstResponse = responses.first()
        responses.forEach { response ->
            assertThat(response).isEqualTo(firstResponse)
        }
    }

    @Test
    fun `multiple users send requests to book the same concert seat but only one booking succeeds`() {

        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val concertId = "concert123"
        val seatId = "A1"

        val request = """
        {
            "concertId": "$concertId",
            "seatId": "$seatId"
        }
    """.trimIndent()

        val responses = mutableListOf<String>()

        for (i in 0 until numberOfThreads) {
            executorService.submit {
                try {
                    // Each user has a unique token
                    val token = UUID.randomUUID()

                    // Send booking request
                    val response = mockMvc.post("/api/concert/book") {
                        contentType = MediaType.APPLICATION_JSON
                        content = request
                        header("token", token.toString())
                    }.andExpect { status { isOk() } }
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

        val successResponses = responses.filter { it.contains("\"code\":\"success\"") }
        val failureResponses = responses.filter { it.contains("\"code\":\"failure\"") }

        assertThat(successResponses.size).isEqualTo(1)

        assertThat(failureResponses.size).isEqualTo(numberOfThreads - 1)
    }


    @Test
    fun `same user sends multiple simultaneous payment requests but only one should succeed`() {
        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val token = UUID.randomUUID()
        val reservationId = "reservation-id-123"

        val request = """
            {
                "id": "$reservationId"
            }
        """.trimIndent()

        val responses = mutableListOf<String>()

        for (i in 0 until numberOfThreads) {
            executorService.submit {
                try {
                    // Payment request
                    val response = mockMvc.post("/api/concert/pay") {
                        contentType = MediaType.APPLICATION_JSON
                        content = request
                        header("token", token.toString())
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

        val successResponses = responses.filter { response ->
            response.contains("\"code\":\"success\"")
        }

        assertThat(successResponses.size).isEqualTo(1)

        val failureResponses = responses.filter { response ->
            response.contains("\"code\":\"failure\"") || response.contains("\"code\":\"duplicate\"")
        }

        assertThat(failureResponses.size).isEqualTo(numberOfThreads - 1)
    }
}
