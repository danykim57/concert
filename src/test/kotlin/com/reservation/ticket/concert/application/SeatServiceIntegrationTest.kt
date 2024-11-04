package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.service.SeatService
import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.domain.Seat
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.SeatRepository
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest
class SeatServiceIntegrationTest(
    @Autowired val seatService: SeatService,
    @Autowired val seatRepository: SeatRepository,
    @Autowired val concertRepository: ConcertRepository
) {

    private val redisContainer = GenericContainer(DockerImageName.parse("redis:7.0.5")).apply {
        withExposedPorts(6379)
    }

    private lateinit var redisTemplate: RedisTemplate<String, String>

    @BeforeEach
    fun setUpRedis() {
        redisContainer.start()

        val redisHost = redisContainer.host
        val redisPort = redisContainer.getMappedPort(6379)

        // Configure Redis connection factory
        val redisConnectionFactory: RedisConnectionFactory = LettuceConnectionFactory(redisHost, redisPort)
        redisTemplate = StringRedisTemplate(redisConnectionFactory as LettuceConnectionFactory)
        redisConnectionFactory.afterPropertiesSet()
    }

    @AfterEach
    fun tearDown() {
        redisContainer.stop()
    }

    @BeforeEach
    fun setup() {
        // 콘서트 생성
        val concert = concertRepository.save(
            Concert(
                name = "Test Concert",
                availableTickets = 50,
                location = "Chicago",
                date = LocalDateTime.of(2040, 12, 31, 19, 0),
            )
        )

        // 좌석 1 생성 및 저장
        val seat1 = Seat(
            seatNumber = "A1",
            isAvailable = true,
            concert = concert,
            price = 10.0
        )
        seatRepository.save(seat1)

        // 좌석 2 생성 및 저장
        val seat2 = Seat(
            seatNumber = "A2",
            isAvailable = true,
            concert = concert,
            price = 10.0
        )
        seatRepository.save(seat2)
    }

    @Test
    fun getAvailableSeats() {
        val date = LocalDateTime.of(2050, 12, 31, 23, 59)
        val seats = seatService.getAvailableSeats(date)
        println("size: " + seats.size)
        assertTrue(seats.isNotEmpty())
    }

    @Test
    fun `50 users try saveWithDistributedLock and only one person succeed to reserve the seat`() {
        // given: get the seat1 from SetUp function
        val seat = seatService.get(1L);

        // when: 50 users call saveWithDistributedLock
        val userCount = 50
        val latch = CountDownLatch(1)
        val completionLatch = CountDownLatch(userCount)
        val successCount = AtomicInteger(0)
        val executorService = Executors.newFixedThreadPool(userCount)

        repeat(userCount) {
            executorService.submit {
                try {
                    latch.await()
                    seatService.saveWithDistributedLock(seat!!.id)
                    successCount.incrementAndGet()
                } catch (e: UnprocessableEntityException) {
                } finally {
                    completionLatch.countDown()
                }
            }
        }

        latch.countDown()
        completionLatch.await(10, TimeUnit.SECONDS)

        // then: only one request should succeed
        assertEquals(1, successCount.get(), "Only one request should succeed")
        executorService.shutdown()
    }
}