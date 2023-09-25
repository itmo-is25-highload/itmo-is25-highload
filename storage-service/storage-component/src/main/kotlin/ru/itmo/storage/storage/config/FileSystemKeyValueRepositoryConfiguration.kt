package ru.itmo.storage.storage.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
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
class FileSystemKeyValueRepositoryConfiguration
