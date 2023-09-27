package ru.itmo.storage.storage.exception

class KeyNotFoundException(
    key: String,
) : StorageComponentException(
    "Key %s is not found!",
    key,
)
