package ru.itmo.storage.storage.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [StorageCoreLibrary::class])
class StorageCoreLibrary

fun main(args: Array<String>) {
    runApplication<StorageCoreLibrary>(*args)
}
