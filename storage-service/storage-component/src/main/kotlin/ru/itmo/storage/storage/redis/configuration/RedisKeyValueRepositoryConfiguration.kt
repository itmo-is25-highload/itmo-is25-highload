package ru.itmo.storage.storage.redis.configuration

import io.lettuce.core.ReadFrom
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.reactive.RedisStringReactiveCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.masterreplica.MasterReplica
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection
import io.lettuce.core.resource.ClientResources
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.redis.properties.RedisProperties
import ru.itmo.storage.storage.redis.repository.RedisKeyValueRepository

@Configuration
@Import(
    RedisKeyValueRepository::class,
)
@EnableConfigurationProperties(RedisProperties::class)
class RedisKeyValueRepositoryConfiguration {

    @Bean("redis-primary-commands")
    fun redisPrimaryReactiveCommands(redisClient: RedisClient): RedisStringReactiveCommands<String?, String?>? {
        return redisClient.connect().reactive()
    }

    @Bean("redis-primary-client")
    fun redisClient(redisProperties: RedisProperties): RedisClient {
        val redisPrimaryURI: RedisURI = RedisURI.builder()
            .withHost(redisProperties.host)
            .withPort(redisProperties.port)
            .withPassword(redisProperties.password)
            .build()

        return RedisClient.create(
            ClientResources.builder().build(),
            redisPrimaryURI,
        )
    }

    @Bean("redis-replica-commands")
    fun redisReplicaReactiveCommands(redisProperties: RedisProperties): RedisStringReactiveCommands<String?, String?>? {
        val redisPrimaryURI: RedisURI = RedisURI.builder()
            .withHost(redisProperties.host)
            .withPort(redisProperties.port)
            .withPassword(redisProperties.password)
            .build()
        val redisClient: RedisClient = RedisClient.create(
            redisPrimaryURI,
        )

        val nodes: List<RedisURI> = listOf(
            RedisURI.builder()
                .withHost("localhost")
                .withPort(6380)
                .withPassword("kek123")
                .build(),
            RedisURI.builder()
                .withHost("localhost")
                .withPort(6381)
                .withPassword("kek123")
                .build(),
            RedisURI.builder()
                .withHost("localhost")
                .withPort(6382)
                .withPassword("kek123")
                .build(),
            // RedisURI.create("redis://localhost:6381"),
            // RedisURI.create("redis://localhost:6382"),
        )
        val primaryAndReplicaConnection: StatefulRedisMasterReplicaConnection<String, String> =
            MasterReplica.connect(
                redisClient,
                StringCodec.UTF8,
                nodes,
            )
        primaryAndReplicaConnection.readFrom = ReadFrom.REPLICA
        return primaryAndReplicaConnection.reactive()
    }
}
