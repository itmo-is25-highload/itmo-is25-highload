package ru.itmo.storage.storage.local

import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.KeyValueRepository

@Repository
class FileSystemKeyValueRepository : KeyValueRepository {

    override fun get(key: String): String {
        TODO("Not yet implemented")
    }

    override fun set(key: String, value: String) {
        TODO("Not yet implemented")
    }
}
