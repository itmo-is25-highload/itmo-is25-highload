package ru.itmo.storage.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.config.FileSystemKeyValueRepositoryConfiguration

@SpringBootApplication(scanBasePackageClasses = [StorageServerApp::class])
@Import(FileSystemKeyValueRepositoryConfiguration::class)
class StorageServerApp

fun main(args: Array<String>) {
    runApplication<StorageServerApp>(*args)
}
