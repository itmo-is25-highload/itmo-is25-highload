package ru.itmo.storage.client.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import ru.itmo.storage.client.impl.properties.StorageClientImplProperties

// TODO: add autoconfiguration
@Configuration
@EnableConfigurationProperties(StorageClientImplProperties::class)
class StorageClientImplConfiguration(
    private val storageClientProperties: StorageClientImplProperties
) {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(storageClientProperties.basePath)
            .build()
    }
}