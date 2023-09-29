package ru.itmo.storage.client.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import ru.itmo.storage.client.impl.properties.StorageClientImplProperties

@Configuration
@EnableConfigurationProperties(StorageClientImplProperties::class)
@ConditionalOnProperty(prefix = "storage.client.baseUrl")
class StorageClientImplConfiguration(
    @Autowired private val storageClientProperties: StorageClientImplProperties
) {
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(storageClientProperties.basePath)
            .build()
    }
}