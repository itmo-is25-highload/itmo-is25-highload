package ru.itmo.storage.storage.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.compression.DeflateCompressionService
import ru.itmo.storage.storage.compression.properties.DeflateCompressionServiceProperties
import ru.itmo.storage.storage.local.FileSystemKeyValueRepository
import ru.itmo.storage.storage.local.properties.FileSystemRepositoryProperties
import ru.itmo.storage.storage.lsm.DefaultMemtableService
import ru.itmo.storage.storage.lsm.LsmTreeKeyValueRepository
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import ru.itmo.storage.storage.lsm.properties.LsmTreeRepositoryProperties
import ru.itmo.storage.storage.lsm.sstable.LocalSSTableLoader
import ru.itmo.storage.storage.lsm.sstable.SSTableManagerImpl

@Configuration
class StorageComponentAutoconfiguration {
    @Import(
        FileSystemKeyValueRepository::class,
    )
    @EnableConfigurationProperties(FileSystemRepositoryProperties::class)
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "local", matchIfMissing = false)
    class FileSystemKeyValueRepositoryLocalConfiguration

    @Import(
        LsmTreeKeyValueRepository::class,
        SSTableManagerImpl::class,
        LocalSSTableLoader::class,
    )
    @EnableConfigurationProperties(LsmTreeRepositoryProperties::class)
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "lsm", matchIfMissing = false)
    class LsmTreeKeyValueRepositoryLocalConfiguration

    @Import(
        DeflateCompressionService::class,
    )
    @EnableConfigurationProperties(DeflateCompressionServiceProperties::class)
    @ConditionalOnProperty(name = ["storage.component.compression.type"], havingValue = "deflate", matchIfMissing = false)
    class DeflateCompressionServiceConfiguration

    @Import(
        DefaultMemtableService::class,
    )
    @EnableConfigurationProperties(LsmRepositoryFlushProperties::class)
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "lsm", matchIfMissing = false)
    class LsmRepositoryMemtableServiceConfiguration
}
