package ru.itmo.ratelimit.server.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.itmo.contracts.ratelimit.RateLimitIncrementRequest
import ru.itmo.contracts.ratelimit.RateLimiterResponse
import ru.itmo.ratelimit.component.services.RateLimiterService

@RestController
@Validated
class RateLimiterController(@Autowired private val rateLimiterService: RateLimiterService) {
    private val log = KotlinLogging.logger { }

    @GetMapping("/checkLimit/{key}")
    fun checkLimit(
        @PathVariable
        @NotBlank
        key: String,
        @RequestParam(required = false)
        value: String?,
    ): RateLimiterResponse {
        log.info { "Rate-Limiter-Service: request to limit $key/$value" }
        val result = runBlocking { rateLimiterService.checkLimit(key, value) }
        return result
    }

    @PostMapping("/incrementRequests")
    fun incrementRequests(
        @RequestBody
        request: RateLimitIncrementRequest,
    ) {
        return runBlocking { rateLimiterService.incrementRequests(request.key, request.value) }
    }
}
