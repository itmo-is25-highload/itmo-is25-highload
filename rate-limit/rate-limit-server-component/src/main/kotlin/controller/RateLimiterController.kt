package ru.itmo.ratelimit.server.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.itmo.ratelimit.component.services.RateLimiterService

@RestController
@Validated
class RateLimiterController(@Autowired private val rateLimiterService: RateLimiterService) {
    private val log = KotlinLogging.logger { }

    @GetMapping("/limit/{key}")
    fun limit(
        @PathVariable
        @NotBlank
        key: String,
        @RequestParam
        value: String?,
    ): Boolean {
        log.info { "Rate-Limiter-Service: request to limit $key/$value" }
        return runBlocking { rateLimiterService.limitRequest(key, value) }
    }
}
