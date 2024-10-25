package com.reservation.ticket.concert.config

import com.reservation.ticket.concert.infrastructure.interceptor.QueueStatusInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(private val queueStatusInterceptor: QueueStatusInterceptor) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 모든 경로에 대해 인터셉터 적용
        registry.addInterceptor(queueStatusInterceptor)
            .addPathPatterns("/api/concert/book")
            .addPathPatterns("/api/concert/pay")
    }
}