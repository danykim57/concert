package com.reservation.ticket.concert.infrastructure.filter

import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper


class LoggingFilterTest {

    private lateinit var loggingFilter: LoggingFilter

    @BeforeEach
    fun setUp() {
        loggingFilter = LoggingFilter()
    }

    @Test
    fun `test request filter keep the request body`() {

        val request = MockHttpServletRequest()
        request.method = "POST"
        request.requestURI = "/test"
        request.addParameter("name", "John Doe")
        request.setContent("""{"key": "value"}""".toByteArray())

        val response = MockHttpServletResponse()

        val filterChain = object : MockFilterChain() {
            override fun doFilter(req: ServletRequest, res: ServletResponse) {
                // 요청 본문을 강제로 읽어 캐시를 생성함
                val wrappedRequest = req as ContentCachingRequestWrapper
                wrappedRequest.reader.lines().forEach { line ->
                    // 본문을 읽는 로직
                }
                super.doFilter(req, res)  // 필터 체인 계속 진행
            }
        }
        val cachedRequest = ContentCachingRequestWrapper(request)
        val cachedResponse = ContentCachingResponseWrapper(response)

        loggingFilter.doFilterInternal(cachedRequest, cachedResponse, filterChain)

        val requestBody = String(cachedRequest.contentAsByteArray!!)

        assertEquals("""{"key": "value"}""", requestBody)
        assertEquals(200, response.status)

    }
}