package ru.itmo.storage.storage.rpcproxy.redis

import org.springframework.stereotype.Service
import ru.itmo.storage.client.StorageClient
import ru.itmo.storage.storage.redis.repository.RedisKeyValueRepository

@Service
class RedisClientAdaptor(
    private val redisKeyValueRepository: RedisKeyValueRepository,
) : StorageClient {

    override suspend fun get(key: String): String = redisKeyValueRepository.get(key)

    override suspend fun set(key: String, value: String) = redisKeyValueRepository.set(key, value)
}