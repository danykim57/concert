package com.reservation.ticket.concert.config

import com.reservation.ticket.concert.infrastructure.filter.LoggingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig {

    @Bean
    fun loggingFilter(): FilterRegistrationBean<LoggingFilter> {
        val registrationBean = FilterRegistrationBean<LoggingFilter>()

        registrationBean.filter = LoggingFilter()
        registrationBean.addUrlPatterns("/api/*")
        registrationBean.order = 1

        return registrationBean
    }
}