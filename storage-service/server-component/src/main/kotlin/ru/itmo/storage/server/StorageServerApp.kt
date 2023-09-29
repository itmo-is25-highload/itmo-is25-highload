package ru.itmo.storage.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [StorageServerApp::class])
class StorageServerApp

fun main(args: Array<String>) {
    runApplication<StorageServerApp>(*args)
}
