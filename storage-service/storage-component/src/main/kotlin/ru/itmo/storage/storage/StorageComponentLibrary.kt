package ru.itmo.storage.storage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [StorageComponentLibrary::class])
class StorageComponentLibrary

fun main(args: Array<String>) {
    runApplication<StorageComponentLibrary>(*args)
}
