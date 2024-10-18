package com.reservation.ticket.concert.application

import com.reservation.ticket.concert.application.service.UserService
import com.reservation.ticket.concert.domain.Point
import com.reservation.ticket.concert.domain.User
import com.reservation.ticket.concert.infrastructure.PointRepository
import com.reservation.ticket.concert.infrastructure.UserRepository
import com.reservation.ticket.concert.interfaces.request.PointRequest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

class UserServiceTest {
    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Mock
    private lateinit var pointRepository: PointRepository

    init {
        MockitoAnnotations.openMocks(this) // Mockito 초기화
    }

    @Test
    fun `getUserPoints should return points when user exists`() {
        // Given
        val user = User()
        val point = Point(id = 0, user = user, amount = 1000.0)

        // Mocking pointRepository to return the point object
        `when`(pointRepository.findByUserId(user.id)).thenReturn(point)

        // When
        val result = userService.get(user.id)

        // Then
        assertEquals(1000.0, result)
    }

    @Test
    fun `getUserPoints should throw exception when user points do not exist`() {
        // Given
        val userId = UUID.randomUUID()

        //When
        `when`(pointRepository.findByUserId(userId)).thenReturn(null)

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            userService.get(userId)
        }

        assertEquals("해당 유저의 포인트 정보가 존재하지 않습니다.", exception.message)
    }

    @Test
    fun `addPoint should create new point when user has no points`() {
        // Given
        val userId = UUID.randomUUID()
        val request = PointRequest(userId = userId, amount = 500.0)
        val user = User(id = userId)

        // Mocking pointRepository to return null for new user
        `when`(pointRepository.findByUserId(userId)).thenReturn(null)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // When
        userService.add(request)

        // Then
        verify(pointRepository, times(1)).save(any(Point::class.java))
    }

    @Test
    fun `addPoint should increase amount when user already has points`() {
        // Given
        val user = User(UUID.randomUUID(), username = "testUser", password = "password")
        val request = PointRequest(userId = user.id, amount = 500.0)
        val point = Point(amount = 1000.0, user = User(id = user.id))

        // Mocking pointRepository to return existing points
        `when`(pointRepository.findByUserId(user.id)).thenReturn(point)

        // When
        userService.add(request)

        // Then
        assertEquals(1500.0, point.amount)
        verify(pointRepository, times(1)).save(point)
    }
}