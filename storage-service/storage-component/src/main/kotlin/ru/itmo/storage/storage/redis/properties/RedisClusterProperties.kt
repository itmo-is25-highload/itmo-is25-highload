package ru.itmo.storage.storage.redis.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.redis-cluster")
class RedisClusterProperties(
    val nodes: List<RedisProperties>,
)
