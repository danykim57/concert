package com.reservation.ticket.concert.application.service

import com.reservation.ticket.concert.domain.Concert
import com.reservation.ticket.concert.infrastructure.ConcertRepository
import com.reservation.ticket.concert.infrastructure.exception.UnprocessableEntityException
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ConcertService(
    private val concertRepository: ConcertRepository,
    private val cacheManager: CacheManager,
) {
    fun get(name: String): Concert = concertRepository.findByName(name)
        ?: throw UnprocessableEntityException("유효하지 않은 콘서트 입니다.")

    fun get(id: Long): Optional<Concert> = concertRepository.findById(id)

    fun create(concert: Concert): Concert {
        val savedConcert = concertRepository.save(concert)
        val cache = cacheManager.getCache("concert")
        cache?.put(savedConcert.name, savedConcert)
       return savedConcert
    }

    fun getAvailableConcerts(): List<Concert> {

        val cache = cacheManager.getCache("concert")
        val cachedConcerts = cache?.get("availableConcerts", List::class.java) as? List<Concert>

        if (cachedConcerts != null) {
            return cachedConcerts
        }

        // 캐시에 없으면 DB에서 조회 후 캐시에 저장
        val availableConcerts = concertRepository.findAvailableConcerts()
        cache?.put("availableConcerts", availableConcerts)

        return availableConcerts
    }

}