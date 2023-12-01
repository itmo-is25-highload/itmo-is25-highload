package ru.itmo.storage.storage.rpcproxy.config

import io.lettuce.core.ReadFrom
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.masterreplica.MasterReplica
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection
import io.lettuce.core.resource.ClientResources
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import ru.itmo.storage.client.impl.StorageClientImpl
import ru.itmo.storage.storage.redis.repository.RedisKeyValueRepository
import ru.itmo.storage.storage.rpcproxy.client.ClientAdaptor
import ru.itmo.storage.storage.rpcproxy.properties.RedisShardProperties
import ru.itmo.storage.storage.rpcproxy.properties.StorageRpcProxyProperties
import ru.itmo.storage.storage.rpcproxy.redis.RedisClientAdaptor
import ru.itmo.storage.storage.rpcproxy.repository.RpcProxyKeyValueRepository
import ru.itmo.storage.storage.rpcproxy.service.ClientProvider
import ru.itmo.storage.storage.rpcproxy.service.impl.ConsistentHashingClientProvider
import ru.itmo.storage.storage.rpcproxy.service.impl.ConsistentHashingEntry

@Import(
    RpcProxyKeyValueRepository::class,
)
@Configuration
@EnableConfigurationProperties(
    StorageRpcProxyProperties::class,
)
class StorageRpcProxyConfiguration(
    private val storageRpcProxyProperties: StorageRpcProxyProperties,
) {

    @Bean
    fun clientProvider(): ClientProvider {
        val redisEntries = buildRedisEntries()
        val lsmEntries = buildLsmEntries()

        return ConsistentHashingClientProvider(
            entries = redisEntries + lsmEntries,
        )
    }

    private fun buildLsmEntries() = storageRpcProxyProperties
        .lsmShards
        .map { shard ->
            val webClientMaster = buildWebClient(shard.master.host, shard.master.port)
            val webClientSlaves = shard.slaves.map {
                buildWebClient(it.host, it.port)
            }

            val clientAdaptor = ClientAdaptor(
                masterStorageClient = StorageClientImpl(webClientMaster),
                slaveStorageClients = webClientSlaves.map { StorageClientImpl(it) },
            )
            ConsistentHashingEntry(
                rangeEnd = shard.rangeEnd,
                client = clientAdaptor,
            )
        }

    private fun buildWebClient(host: String, port: Int) = WebClient.builder()
        .baseUrl(
            UriComponentsBuilder.newInstance()
                .host(host)
                .port(port)
                .build()
                .toUriString(),
        )
        .build()

    private fun buildRedisEntries(): Collection<ConsistentHashingEntry> = storageRpcProxyProperties
        .redisShards
        .map {
            val masterCommands = buildMasterRedisCommands(it)
            val replicaCommands = buildReplicaRedisCommands(it)

            val repository = RedisKeyValueRepository(masterCommands, replicaCommands)

            ConsistentHashingEntry(
                rangeEnd = it.rangeEnd,
                client = RedisClientAdaptor(repository),
            )
        }

    private fun buildMasterRedisCommands(it: RedisShardProperties): RedisReactiveCommands<String, String> {
        val redisPrimaryURI: RedisURI = RedisURI.builder()
            .withHost(it.master.host)
            .withPort(it.master.port)
            .withPassword(it.password)
            .build()

        return RedisClient.create(
            ClientResources.builder().build(),
            redisPrimaryURI,
        ).connect().reactive()
    }

    private fun buildReplicaRedisCommands(properties: RedisShardProperties): RedisReactiveCommands<String, String> {
        val redisPrimaryURI: RedisURI = RedisURI.builder()
            .withHost(properties.master.host)
            .withPort(properties.master.port)
            .withPassword(properties.password)
            .build()
        val redisClient: RedisClient = RedisClient.create(
            redisPrimaryURI,
        )

        val slaveNodes = properties.slaves.map {
            RedisURI.builder()
                .withHost(it.host)
                .withPort(it.port)
                .withPassword(properties.password)
                .build()
        }
        val masterNode = RedisURI.builder()
            .withHost(properties.master.host)
            .withPort(properties.master.port)
            .withPassword(properties.password)
            .build()

        val primaryAndReplicaConnection: StatefulRedisMasterReplicaConnection<String, String> =
            MasterReplica.connect(
                redisClient,
                StringCodec.UTF8,
                slaveNodes + masterNode,
            )
        primaryAndReplicaConnection.readFrom = ReadFrom.REPLICA_PREFERRED
        return primaryAndReplicaConnection.reactive()
    }
}
