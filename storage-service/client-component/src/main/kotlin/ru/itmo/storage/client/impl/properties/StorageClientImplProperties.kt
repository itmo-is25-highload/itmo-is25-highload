package ru.itmo.storage.client.impl.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.client")
class StorageClientImplProperties {
    lateinit var basePath: String
}