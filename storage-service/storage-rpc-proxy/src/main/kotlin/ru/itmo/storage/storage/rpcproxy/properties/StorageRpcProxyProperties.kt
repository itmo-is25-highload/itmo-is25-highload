package ru.itmo.storage.storage.rpcproxy.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.rpcproxy.shards")
data class StorageRpcProxyProperties(
    val redisShards: Collection<RedisShardProperties> = emptyList(),
    val lsmShards: Collection<LsmShardProperties> = emptyList(),
)

sealed interface ShardProperties {
    val rangeEnd: Long
}

data class RedisShardProperties(
    override val rangeEnd: Long,
    val password: String,
    val master: HostProperties,
    val slaves: Collection<HostProperties> = emptyList(),
) : ShardProperties

data class LsmShardProperties(
    override val rangeEnd: Long,
    val master: HostProperties,
    val slaves: Collection<HostProperties> = emptyList(),
) : ShardProperties

data class HostProperties(
    val host: String,
    val port: Int,
)