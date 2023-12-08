package ru.itmo.storage.storage.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.compression.DeflateCompressionService
import ru.itmo.storage.storage.compression.properties.DeflateCompressionServiceProperties
import ru.itmo.storage.storage.identifiers.config.UniqueIdentifiersConfig
import ru.itmo.storage.storage.jobs.config.QuartzConfig
import ru.itmo.storage.storage.local.FileSystemKeyValueRepository
import ru.itmo.storage.storage.local.properties.FileSystemRepositoryProperties
import ru.itmo.storage.storage.lsm.DefaultMemtableService
import ru.itmo.storage.storage.lsm.LsmTreeKeyValueRepository
import ru.itmo.storage.storage.lsm.core.bloomfilter.BloomFilterProperties
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import ru.itmo.storage.storage.lsm.properties.LsmTreeRepositoryProperties
import ru.itmo.storage.storage.lsm.sstable.LocalSSTableLoader
import ru.itmo.storage.storage.lsm.sstable.SSTableManagerImpl
import ru.itmo.storage.storage.redis.configuration.RedisClusterKeyValueRepositoryConfiguration
import ru.itmo.storage.storage.redis.configuration.RedisKeyValueRepositoryConfiguration
import ru.itmo.storage.storage.rpcproxy.config.StorageRpcProxyConfiguration
import ru.itmo.storage.storage.lsm.core.wal.WalConfig

@EnableAspectJAutoProxy
@Configuration
@Import(
    CoroutinesConfiguration::class,
    WalConfig::class,
    UniqueIdentifiersConfig::class,
)
class StorageComponentAutoconfiguration {

    @Import(
        FileSystemKeyValueRepository::class,
    )
    @EnableConfigurationProperties(FileSystemRepositoryProperties::class)
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "local", matchIfMissing = false)
    class FileSystemKeyValueRepositoryLocalConfiguration

    @Import(
        LsmTreeKeyValueRepository::class,
        LocalSSTableLoader::class,
        QuartzConfig::class,
    )
    @EnableConfigurationProperties(LsmTreeRepositoryProperties::class, BloomFilterProperties::class)
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "lsm", matchIfMissing = false)
    class LsmTreeKeyValueRepositoryLocalConfiguration

    @Import(
        SSTableManagerImpl::class,
    )
    @EnableConfigurationProperties(BloomFilterProperties::class)
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "lsm", matchIfMissing = false)
    class LsmTreeKeyValueRepositorySSTableConfiguration

    @Import(
        DeflateCompressionService::class,
    )
    @EnableConfigurationProperties(DeflateCompressionServiceProperties::class)
    @ConditionalOnProperty(
        name = ["storage.component.compression.type"],
        havingValue = "deflate",
        matchIfMissing = false,
    )
    class DeflateCompressionServiceConfiguration

    @Import(
        DefaultMemtableService::class,
    )
    @EnableConfigurationProperties(LsmRepositoryFlushProperties::class)
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "lsm", matchIfMissing = false)
    class LsmRepositoryMemtableServiceConfiguration

    @Import(
        RedisKeyValueRepositoryConfiguration::class,
    )
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "redis", matchIfMissing = false)
    class RedisRepositoryServiceConfiguration

    @Import(
        RedisClusterKeyValueRepositoryConfiguration::class,
    )
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "redis-cluster", matchIfMissing = false)
    class RedisClusterRepositoryConfiguration

    @Configuration
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "rpc-proxy", matchIfMissing = false)
    @Import(
        StorageRpcProxyConfiguration::class,
    )
    class StorageRpcProxyAutoConfiguration
}
