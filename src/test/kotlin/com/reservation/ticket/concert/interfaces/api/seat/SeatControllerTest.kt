package com.reservation.ticket.concert.interfaces.api.seat

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class SeatControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `multiple users send requests to get available seats`() {
        // 동시성 테스트를 위한 설정
        val numberOfThreads = 10
        val executorService: ExecutorService = Executors.newFixedThreadPool(numberOfThreads)
        val latch = CountDownLatch(numberOfThreads)

        // 테스트용 concertId와 date 설정
        val concertId = "concert123"
        val date = LocalDateTime.now().plusDays(1) // 현재 시간의 1일 후 날짜

        // 응답 저장용 리스트
        val responses = mutableListOf<String>()

        // 10명의 사용자로부터 동시 요청을 보냄
        for (i in 0 until numberOfThreads) {
            executorService.submit {
                try {
                    // 좌석 조회 요청
                    val response = mockMvc.get("/api/seat/$concertId?date=$date")
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

        // 모든 스레드가 작업을 마칠 때까지 대기
        latch.await()

        // 응답 결과 확인
        assertThat(responses.size).isEqualTo(numberOfThreads)

        // 첫 번째 응답과 나머지 응답이 동일한지 확인
        val firstResponse = responses.first()
        responses.forEach { response ->
            assertThat(response).isEqualTo(firstResponse)
        }
    }
}
