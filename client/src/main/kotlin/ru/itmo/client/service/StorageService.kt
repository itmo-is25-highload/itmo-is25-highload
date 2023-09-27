package ru.itmo.client.service

interface StorageService {
    fun getValue(key: String)
    fun setValue(key: String, value: String)
}
