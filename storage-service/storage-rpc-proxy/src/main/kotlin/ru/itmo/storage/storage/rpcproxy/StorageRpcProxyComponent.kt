package ru.itmo.storage.storage.rpcproxy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [StorageRpcProxyComponent::class])
class StorageRpcProxyComponent

fun main(args: Array<String>) {
    runApplication<StorageRpcProxyComponent>(*args)
}
