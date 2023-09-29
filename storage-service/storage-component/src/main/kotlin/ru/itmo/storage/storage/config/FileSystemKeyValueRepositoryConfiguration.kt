package ru.itmo.storage.storage.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.local.FileSystemKeyValueRepository
import ru.itmo.storage.storage.local.properties.FileSystemRepositoryProperties

// TODO: add autoconfiguration
@Configuration
@Import(
    FileSystemKeyValueRepository::class,
)
@EnableConfigurationProperties(FileSystemRepositoryProperties::class)
class FileSystemKeyValueRepositoryConfiguration(@Autowired private val repositoryProperties: FileSystemRepositoryProperties)
{
    @Bean
    @ConditionalOnProperty(prefix = "storage.component.filesystem.type", havingValue = "local" , matchIfMissing = false)
    fun localFileSystemKeyValueRepository(): FileSystemKeyValueRepository {
        return FileSystemKeyValueRepository(repositoryProperties)
    }

}
