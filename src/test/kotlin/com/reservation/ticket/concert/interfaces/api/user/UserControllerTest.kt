package com.reservation.ticket.concert.interfaces.api.user

import com.reservation.ticket.concert.application.service.QueueService
import org.junit.jupiter.api.Test

import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var queueService: QueueService

    @Test
    fun `getUserQueuePosition should return position successfully`() {
        // Given
        val userId = UUID.randomUUID()
        val position = 3

        // Mocking the service
        `when`(queueService.getUserQueuePosition(userId)).thenReturn(position)

        // When & Then
        mockMvc.perform(get("/queue/$userId/position"))
            .andExpect(status().isOk)
            .andExpect(content().string("3"))
    }

    @Test
    fun `getUserQueuePosition should return 400 when user not in queue`() {
        // Given
        val userId = UUID.randomUUID()

        // Mocking the service to throw an exception
        `when`(queueService.getUserQueuePosition(userId))
            .thenThrow(IllegalArgumentException("해당 유저는 대기열에 없습니다."))

        // When & Then
        mockMvc.perform(get("/queue/$userId/position"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("해당 유저는 대기열에 없습니다."))
    }
}