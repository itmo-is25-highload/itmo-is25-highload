package ru.itmo.storage.server.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.itmo.storage.storage.core.KeyValueRepository

@RestController
@Validated
class StorageController(
    private val keyValueRepository: KeyValueRepository,
) {

    private val log = KotlinLogging.logger { }

    @GetMapping("/keys/{key}")
    fun getByKey(
        @PathVariable
        @NotBlank
        key: String,
    ): String {
        val value: String = runBlocking { keyValueRepository.get(key) }
        log.info { "Got value {$value} by key {$key}" }

        return value
    }

    @PutMapping("/keys/{key}")
    fun setKey(
        @PathVariable
        @NotBlank
        key: String,
        @RequestParam
        value: String,
    ) {
        log.info { "Set value $value to key $key" }

        runBlocking { keyValueRepository.set(key, value) }

        log.info { "Set value $value to key $key finished" }
    }
}
