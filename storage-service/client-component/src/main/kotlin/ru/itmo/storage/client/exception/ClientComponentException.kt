package ru.itmo.storage.client.exception

abstract class ClientComponentException(
    messageFormat: String,
    vararg params: Any,
) : RuntimeException(String.format(messageFormat, params))