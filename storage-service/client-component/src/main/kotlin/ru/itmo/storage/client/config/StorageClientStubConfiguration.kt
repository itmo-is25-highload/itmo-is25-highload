package ru.itmo.storage.client.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.client.stub.StorageClientStub

@Configuration
@Import(
    StorageClientStub::class,
)
@ConditionalOnProperty(prefix = "storage.client.baseUrl" , matchIfMissing = true)
class StorageClientStubConfiguration
