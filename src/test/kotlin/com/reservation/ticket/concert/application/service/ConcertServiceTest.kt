package com.reservation.ticket.concert.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConcertServiceTest(
    @Autowired private val concertService: ConcertService,
    @Autowired private val concertRepository: ConcertRepository,
    @Autowired private val cacheManager: CacheManager
) {

    @BeforeEach
    fun setUp() {
        // 각 테스트 전에 캐시를 초기화하여 이전 테스트의 영향을 방지합니다.
        cacheManager.getCache("concert")?.clear()
    }

    @Test
    fun `test getAvailableConcerts with caching`() {
        // 테스트용 콘서트 데이터 저장
        val concert1 = concertRepository.save(Concert(name = "Concert A", availableTickets = 10, date = LocalDateTime.now().plusDays(1)))
        val concert2 = concertRepository.save(Concert(name = "Concert B", availableTickets = 5, date = LocalDateTime.now().plusDays(2)))

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())

        // 캐시 미스 상태에서 getAvailableConcerts() 호출 -> DB에서 데이터 가져오고 캐시에 저장
        val availableConcertsFirstCall = concertService.getAvailableConcerts()
        val availableConcertsFirstCallJson = objectMapper.writeValueAsString(availableConcertsFirstCall)
        assertEquals(2, availableConcertsFirstCall.size)

        // 캐시에 데이터가 저장되었는지 확인
        val cache = cacheManager.getCache("concert")
        val cachedConcerts = cache?.get("availableConcerts", List::class.java) as? List<Concert>
        assertNotNull(cachedConcerts)
        assertEquals(2, cachedConcerts?.size)

        // 캐시된 데이터를 이용해 getAvailableConcerts() 호출 -> 캐시에서 가져오기 때문에 DB 조회 없음
        val availableConcertsSecondCall = concertService.getAvailableConcerts()
        assertEquals(2, availableConcertsSecondCall.size)


        // 캐시를 초기화하고 다시 호출하여 캐시가 갱신되었는지 확인
        cache?.evict("availableConcerts")
        val availableConcertsAfterEviction = concertService.getAvailableConcerts()
        assertEquals(2, availableConcertsAfterEviction.size)
        assertNotSame(availableConcertsFirstCallJson, availableConcertsAfterEviction, "캐시가 초기화된 후 새로운 데이터를 가져와야 합니다")
    }
}