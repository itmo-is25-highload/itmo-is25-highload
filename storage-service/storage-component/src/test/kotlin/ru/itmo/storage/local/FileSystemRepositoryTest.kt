package ru.itmo.storage.local

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.itmo.storage.storage.KeyValueRepository
import ru.itmo.storage.storage.config.StorageComponentAutoconfiguration
import ru.itmo.storage.storage.exception.KeyNotFoundException

@SpringBootTest(classes = [StorageComponentAutoconfiguration::class])
class FileSystemRepositoryTest(
    @Autowired private val keyValueRepository: KeyValueRepository,
) {

    @Test
    fun `set key - success`() {
        keyValueRepository.set("hello", "hi")
    }

    @Test
    fun `get key - success`() {
        val value = keyValueRepository.get("hello")

        Assertions.assertEquals("hi", value)
    }

    @Test
    fun `get key - not found`() {
        assertThrows<KeyNotFoundException> {
            keyValueRepository.get("hi")
        }
    }
}
