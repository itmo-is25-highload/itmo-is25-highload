package ru.itmo.storage.storage.core

interface KeyValueRepository {

    suspend fun get(key: String): String

    suspend fun set(key: String, value: String)

    fun reload() {}
}
