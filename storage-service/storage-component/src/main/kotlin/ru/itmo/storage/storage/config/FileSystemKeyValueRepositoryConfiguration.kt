package ru.itmo.storage.storage.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.local.FileSystemKeyValueRepository
import ru.itmo.storage.storage.local.properties.FileSystemRepositoryProperties

@Configuration
@EnableConfigurationProperties(FileSystemRepositoryProperties::class)
class FileSystemKeyValueRepositoryConfiguration {
    @Import(
        FileSystemKeyValueRepository::class,
    )
    @ConditionalOnProperty(name = ["storage.component.filesystem.type"], havingValue = "local", matchIfMissing = false)
    class FileSystemKeyValueRepositoryLocalConfiguration
}
