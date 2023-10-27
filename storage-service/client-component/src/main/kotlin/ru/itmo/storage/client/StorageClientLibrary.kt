package ru.itmo.storage.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [StorageClientLibrary::class])
class StorageClientLibrary

fun main(args: Array<String>) {
    runApplication<StorageClientLibrary>(*args)
}
