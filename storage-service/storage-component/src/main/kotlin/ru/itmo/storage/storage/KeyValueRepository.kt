package ru.itmo.storage.storage

interface KeyValueRepository {

    suspend fun get(key: String): String

    suspend fun set(key: String, value: String)
}
