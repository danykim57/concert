package com.reservation.ticket.concert.infrastructure.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(val lockKey: String, val timeout: Long = 5000)
