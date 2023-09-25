package ru.itmo.storage.client.stub

import org.springframework.stereotype.Component
import ru.itmo.storage.client.StorageClient

@Component
class StorageClientStub : StorageClient {

    override fun get(key: String): String {
        throw NotImplementedError()
    }

    override fun set(key: String, value: String) {
        throw NotImplementedError()
    }
}
