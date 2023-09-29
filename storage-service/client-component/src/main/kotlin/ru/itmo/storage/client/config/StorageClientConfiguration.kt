package ru.itmo.storage.client.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClient
import ru.itmo.storage.client.impl.StorageClientImpl
import ru.itmo.storage.client.impl.properties.StorageClientImplProperties
import ru.itmo.storage.client.stub.StorageClientStub

@Configuration
class StorageClientConfiguration {

    @Configuration
    @Import(
        StorageClientImpl::class,
    )
    @EnableConfigurationProperties(StorageClientImplProperties::class)
    @ConditionalOnProperty(name = ["storage.client.type"], havingValue = "spring", matchIfMissing = false)
    class StorageClientImplConfiguration {
        @Bean
        fun webClient(storageClientProperties: StorageClientImplProperties): WebClient {
            return WebClient.builder()
                .baseUrl(storageClientProperties.basePath)
                .build()
        }
    }

    @Configuration
    @Import(
        StorageClientStub::class,
    )
    @ConditionalOnProperty(name = ["storage.client.type"], havingValue = "stub", matchIfMissing = false)
    class StorageClientStubConfiguration
}
