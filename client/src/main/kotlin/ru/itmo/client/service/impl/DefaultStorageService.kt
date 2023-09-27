package ru.itmo.client.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.client.service.StorageService
import ru.itmo.storage.client.StorageClient

@Service
class DefaultStorageService(
    private val storageClient: StorageClient,
) : StorageService {
    private val log = KotlinLogging.logger { }

    override fun getValue(key: String) {
        runCatching {
            log.info { "Call storageClient.get(key) with params - key: $key" }
            storageClient.get(key)
        }.fold(
            onSuccess = { value ->
                log.info { "Success when call storageClient.get(key)" }
                log.info { "Result for key: $key is value: $value" }
            },
            onFailure = {
                log.info { "Failure when call storageClient.get(key)" }
                log.info { "Stacktrace: ${it.stackTraceToString()}" }
            },
        )
    }

    override fun setValue(key: String, value: String) {
        runCatching {
            log.info { "Call storageClient.set(key, value) with params - key: $key, value: $value" }
            storageClient.set(key, value)
        }.fold(
            onSuccess = {
                log.info { "Success when call storageClient.set(key, value)" }
            },
            onFailure = {
                log.info { "Failure when call storageClient.set(key, value)" }
                log.info { "Stacktrace: ${it.stackTraceToString()}" }
            },
        )
    }
}
