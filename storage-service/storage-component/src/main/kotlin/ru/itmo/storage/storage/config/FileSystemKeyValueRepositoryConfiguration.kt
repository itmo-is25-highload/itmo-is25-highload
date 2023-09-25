package ru.itmo.storage.storage.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.local.FileSystemKeyValueRepository

// TODO: add autoconfiguration
@Configuration
@Import(
    FileSystemKeyValueRepository::class,
)
class FileSystemKeyValueRepositoryConfiguration
