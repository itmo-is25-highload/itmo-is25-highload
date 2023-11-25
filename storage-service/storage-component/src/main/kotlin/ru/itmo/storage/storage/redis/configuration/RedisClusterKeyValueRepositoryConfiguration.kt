package ru.itmo.storage.storage.redis.configuration

import io.lettuce.core.ReadFrom
import io.lettuce.core.RedisURI
import io.lettuce.core.api.reactive.RedisStringReactiveCommands
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.resource.ClientResources
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.redis.properties.RedisClusterProperties
import ru.itmo.storage.storage.redis.repository.RedisKeyValueRepository

@Configuration
@Import(
    RedisKeyValueRepository::class,
)
@EnableConfigurationProperties(RedisClusterProperties::class)
class RedisClusterKeyValueRepositoryConfiguration {

    @Bean("redis-primary-commands")
    fun redisPrimaryReactiveCommands(redisClient: RedisClusterClient): RedisStringReactiveCommands<String?, String?>? {
        return redisClient.connect().reactive()
    }

    @Bean("redis-primary-client")
    fun redisClient(redisClusterProperties: RedisClusterProperties): RedisClusterClient {
        val redisUris = redisClusterProperties.nodes.stream().map { property ->
            RedisURI.builder()
                .withHost(property.host)
                .withPort(property.port)
                .withPassword(property.password)
                .build()
        }.toList()

        return RedisClusterClient.create(
            ClientResources.builder().build(),
            redisUris,
        )
    }

    @Bean("redis-replica-commands")
    fun redisReplicaReactiveCommands(redisClusterProperties: RedisClusterProperties): RedisStringReactiveCommands<String?, String?>? {
        val redisUris = redisClusterProperties.nodes.stream().map { property ->
            RedisURI.builder()
                .withHost(property.host)
                .withPort(property.port)
                .withPassword(property.password)
                .build()
        }.toList()

        val redisClient: RedisClusterClient = RedisClusterClient.create(
            redisUris,
        )

        val primaryAndReplicaConnection: StatefulRedisClusterConnection<String, String> = redisClient.connect()

        primaryAndReplicaConnection.readFrom = ReadFrom.REPLICA
        return primaryAndReplicaConnection.reactive()
    }
}
