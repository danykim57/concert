package com.reservation.ticket.concert.config

import com.reservation.ticket.concert.application.service.BulkInsertService
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig(private val bulkInsertService: BulkInsertService) {

    @Bean
    fun initData(): ApplicationRunner {
        return ApplicationRunner {
            bulkInsertService.bulkInsertConcerts(1_000_000)
            bulkInsertService.bulkInsertUsers(1_000_000)
            bulkInsertService.bulkInsertPoints(1_000_000)
        }
    }
}