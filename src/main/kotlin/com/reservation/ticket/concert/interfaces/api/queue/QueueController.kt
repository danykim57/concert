package com.reservation.ticket.concert.interfaces.api.queue

import com.reservation.ticket.concert.application.service.QueueService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/queue")
class QueueController(private val queueService: QueueService) {

    @PostMapping("/add")
    fun addUserToQueue(@RequestParam userId: String) {
        queueService.addUserToWaitQueue(userId, System.currentTimeMillis().toDouble())
    }

    @GetMapping("/rank")
    fun getUserRank(@RequestParam userId: String): Long? {
        return queueService.getUserRankInQueue(userId)
    }

    @GetMapping("/active")
    fun getActiveUsers(): Set<String> {
        return queueService.getActiveUsers()
    }

    @PostMapping("/complete")
    fun completeUser(@RequestParam userId: String) {
        queueService.completeUserProcess(userId)
    }
}