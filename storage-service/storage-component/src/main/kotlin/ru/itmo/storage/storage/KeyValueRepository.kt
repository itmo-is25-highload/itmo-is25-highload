package ru.itmo.storage.storage

interface KeyValueRepository {

    fun get(key: String): String

    fun set(key: String, value: String)
}
