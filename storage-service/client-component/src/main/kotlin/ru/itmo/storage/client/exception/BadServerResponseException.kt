package ru.itmo.storage.client.exception

class BadServerResponseException(message: String?) : ClientComponentException(
    message ?: ""
)