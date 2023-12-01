package ru.itmo.storage.storage.rpcproxy.client

import org.springframework.stereotype.Service
import ru.itmo.storage.client.StorageClient

@Service
class ClientAdaptor(
    private val masterStorageClient: StorageClient,
    private val slaveStorageClients: Collection<StorageClient>,
) : StorageClient {

    override suspend fun get(key: String): String {
        return slaveStorageClients.random().get(key)
    }

    override suspend fun set(key: String, value: String) {
        masterStorageClient.set(key, value)
    }
}