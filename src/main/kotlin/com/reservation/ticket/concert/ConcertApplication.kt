package com.reservation.ticket.concert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
class ConcertApplication

fun main(args: Array<String>) {
    runApplication<ConcertApplication>(*args)
}
