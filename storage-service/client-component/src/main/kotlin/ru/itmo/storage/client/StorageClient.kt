package ru.itmo.storage.client

interface StorageClient {

    suspend fun get(key: String): String

    suspend fun set(key: String, value: String)
}
