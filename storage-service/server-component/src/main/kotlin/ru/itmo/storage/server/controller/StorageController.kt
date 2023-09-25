package ru.itmo.storage.server.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import ru.itmo.storage.storage.KeyValueRepository

@RestController
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
        log.info { "Context is up!" }

        return "Your value is $key"
    }
}
