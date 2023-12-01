package ru.itmo.storage.storage.rpcproxy.service

import ru.itmo.storage.client.StorageClient

interface ClientProvider {

    fun provide(hash: Long): StorageClient
}
