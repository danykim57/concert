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
class ConcertControllerConcurrencyTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `multiple users send requests to get available concerts`() {
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
    fun `multiple users send requests to book the concert`() {

        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val token = UUID.randomUUID()

        val request = """
            {
                "concertId": "concert123",
                "seatId": "A1"
            }
        """.trimIndent()

        // 응답 저장용 리스트
        val responses = mutableListOf<String>()

        // 10명의 사용자로부터 동시 예약 요청을 보냄
        for (i in 0 until numberOfThreads) {
            executorService.submit {
                try {
                    // 좌석 예약 요청
                    val response = mockMvc.post("/api/concert/book") {
                        contentType = MediaType.APPLICATION_JSON
                        content = request
                        header("token", token)
                    }.andExpect { status { isOk() } }
                        .andReturn().response.contentAsString

                    synchronized(responses) {
                        responses.add(response)
                    }
                } finally {
                    latch.countDown()  // 스레드 작업 완료 알림
                }
            }
        }

        // 모든 스레드가 작업을 마칠 때까지 대기
        latch.await()

        // 응답 결과 확인
        assertThat(responses.size).isEqualTo(numberOfThreads)

        val successResponses = responses.filter { it.contains("\"code\":\"success\"") }
        val failureResponses = responses.filter { it.contains("\"code\":\"failure\"") }

        assertThat(successResponses.size).isEqualTo(1)
        assertThat(failureResponses.size).isEqualTo(numberOfThreads - 1)
    }

    @Test
    fun `multiple users send payment requests simultaneously`() {
        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        val token = UUID.randomUUID()

        val request = """
            {
                "id": "reservation-id-123"
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
}
