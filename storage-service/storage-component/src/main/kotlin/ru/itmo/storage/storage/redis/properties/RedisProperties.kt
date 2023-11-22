package ru.itmo.storage.storage.redis.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.redis")
data class RedisProperties(
    val host: String,
    val port: Int,
    val password: String,
    val username: String,
)
