package ru.itmo.storage.storage.redis.repository

import io.lettuce.core.api.reactive.RedisStringReactiveCommands
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.core.KeyValueRepository
import ru.itmo.storage.storage.core.exception.KeyNotFoundException

@Repository
class RedisKeyValueRepository(
    private val redisPrimaryCommands: RedisStringReactiveCommands<String, String>,
    private val redisReplicaCommands: RedisStringReactiveCommands<String, String>,
) : KeyValueRepository {

    override suspend fun get(key: String): String {
        return redisReplicaCommands
            .get(key)
            .awaitSingleOrNull()
            ?: throw KeyNotFoundException(key)
    }

    override suspend fun set(key: String, value: String) {
        redisPrimaryCommands
            .set(key, value)
            .awaitSingle()
    }
}
