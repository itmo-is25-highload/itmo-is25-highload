package ru.itmo.storage.storage.identifiers

import org.springframework.stereotype.Service
import java.util.*

@Service
class UUIDIdentifierService : UniqueIdentifierService {

    override fun nextIdentifier(): String {
        return UUID.randomUUID().toString()
    }
}
