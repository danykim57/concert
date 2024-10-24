package com.reservation.ticket.concert.infrastructure.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException
import java.nio.charset.StandardCharsets


class LoggingFilter : OncePerRequestFilter() {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(LoggingFilter::class.java)
    }

    @Throws(ServletException::class, IOException::class)
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cachedRequest = ContentCachingRequestWrapper(request)
        val cachedResponse = ContentCachingResponseWrapper(response)

        filterChain.doFilter(cachedRequest, cachedResponse)

        log.info("Request: {} {}", request.method, request.requestURI)

        cachedRequest.parameterMap.forEach { (key, value) ->
            log.info("Request Param: {}={}", key, value.joinToString())
        }

        val requestBody = String(cachedRequest.contentAsByteArray, StandardCharsets.UTF_8)
        log.info("Request Payload: {}", requestBody)

        log.info("Response Status: {}", response.status)

        cachedResponse.copyBodyToResponse()
    }
}