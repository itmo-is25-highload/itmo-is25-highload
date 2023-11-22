package ru.itmo.storage.storage.identifiers

interface UniqueIdentifierService {

    fun nextIdentifier(): String
}
