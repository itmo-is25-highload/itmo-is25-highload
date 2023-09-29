package ru.itmo.storage.storage.exception

abstract class StorageComponentException(
    messageFormat: String,
    vararg params: Any,
) : RuntimeException(String.format(messageFormat, *(params.map { it.toString() }.toTypedArray())))
