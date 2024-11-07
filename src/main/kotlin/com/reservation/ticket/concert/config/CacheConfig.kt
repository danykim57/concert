package com.reservation.ticket.concert.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration


@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun redisCacheManager(redisConnectionFactory: RedisConnectionFactory?): RedisCacheManager {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        val defaultConfig: RedisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer<String>(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer<Any>(
                    GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )
            .disableCachingNullValues()
            .disableKeyPrefix()
            .entryTtl(Duration.ofSeconds(60)) // 캐시의 유효시간 설정

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory!!)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("concert", defaultConfig.entryTtl(Duration.ofHours(24)))
            .withCacheConfiguration("availableConcerts", defaultConfig.entryTtl(Duration.ofHours(24)))
            .withCacheConfiguration("seat", defaultConfig.entryTtl(Duration.ofHours(24)))
            .build()
    }
}
