package com.reservation.ticket.concert.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.backoff.FixedBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate


@Configuration
@EnableRetry
class RetryConfig {

    private val retryIntervalInSeconds = 1

    @Bean
    fun retryTemplate(): RetryTemplate {
        val retryPolicy = SimpleRetryPolicy()
        retryPolicy.maxAttempts = 3

        val backOffPolicy = FixedBackOffPolicy()
        backOffPolicy.backOffPeriod = (retryIntervalInSeconds * 1000).toLong()

        val template = RetryTemplate()
        template.setRetryPolicy(retryPolicy)
        template.setBackOffPolicy(backOffPolicy)

        return template
    }
}