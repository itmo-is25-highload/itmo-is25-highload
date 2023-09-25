package ru.itmo.storage.client

interface StorageClient {

    fun get(key: String): String

    fun set(key: String, value: String)
}
