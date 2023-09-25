package ru.itmo.storage.storage.local

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.KeyValueRepository
import ru.itmo.storage.storage.exception.KeyNotFoundException
import ru.itmo.storage.storage.local.properties.FileSystemRepositoryProperties
import java.io.File

@Repository
class FileSystemKeyValueRepository(
    private val fileSystemRepositoryProperties: FileSystemRepositoryProperties,
) : KeyValueRepository {

    private val log = KotlinLogging.logger { }

    override fun get(key: String): String {
        val path = fileSystemRepositoryProperties.storagePath

        return findFile(path, key)
            ?.readText()
            ?: throw KeyNotFoundException(key)
    }

    override fun set(key: String, value: String) {
        val path = fileSystemRepositoryProperties.storagePath

        createDirectoryIfNotExists(path)

        val storageFile = findFile(path, key) ?: createFile(path, key)

        storageFile.setWritable(true)
        storageFile.writeText(value)
    }

    private fun createDirectoryIfNotExists(path: String) {
        val file = File(path)
        if (!file.isDirectory) {
            file.mkdir()
        }
    }

    private fun findFile(path: String, key: String) = File(path)
        .also { log.info { "Absolute path to search ${it.absolutePath}" } }
        .walk()
        .filter { it.name == key }
        .singleOrNull()

    private fun createFile(path: String, key: String) = File(
        File(path),
        key,
    )
}
