package ru.itmo.storage.server.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import ru.itmo.storage.storage.KeyValueRepository

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

        val value: String = keyValueRepository.get(key)
        log.info { "Got value {$value} by key {$key}" }

        return value
    }

    @PutMapping("/keys/{key}")
    fun setKey(
        @PathVariable
        @NotBlank
        key: String,
        @RequestParam
        value: String
    ) {
        log.info { "Set value {$value} to key {$key}" }

        keyValueRepository.set(key, value)
    }
}

