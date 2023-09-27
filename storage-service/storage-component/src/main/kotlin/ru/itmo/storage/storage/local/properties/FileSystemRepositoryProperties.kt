package ru.itmo.storage.storage.local.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.filesystem")
class FileSystemRepositoryProperties {
    lateinit var storagePath: String
}
