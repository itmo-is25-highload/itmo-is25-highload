package ru.itmo.storage.client.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.client.stub.StorageClientStub

// TODO: add autoconfiguration
@Configuration
@Import(
    StorageClientStub::class,
)
class StorageClientStubConfiguration
