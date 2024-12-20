package com.reservation.ticket.concert.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName


@Configuration
class RedisConfig {
    private val redisContainer = GenericContainer(DockerImageName.parse("redis:7.0.5")).apply {
        withExposedPorts(6379)
        start() // Start the container when the configuration is loaded
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisHost = redisContainer.host
        val redisPort = redisContainer.getMappedPort(6379)
        return LettuceConnectionFactory(redisHost, redisPort).apply {
            afterPropertiesSet()
        }
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        return template
    }
}