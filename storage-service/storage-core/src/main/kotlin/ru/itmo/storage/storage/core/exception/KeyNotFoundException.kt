package ru.itmo.storage.storage.core.exception

class KeyNotFoundException(
    key: String,
) : StorageComponentException(
    "Key %s is not found!",
    key,
)
