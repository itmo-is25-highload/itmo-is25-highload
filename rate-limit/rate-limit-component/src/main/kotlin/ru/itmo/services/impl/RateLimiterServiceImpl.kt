package ru.itmo.ratelimit.component.services

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.ratelimit.component.entries.DescriptorLimit
import ru.itmo.storage.storage.core.KeyValueRepository
import ru.itmo.storage.storage.core.exception.KeyNotFoundException

@Service
class RateLimiterServiceImpl(private val configurationLookupService: RateLimiterConfigurationLookupService, private val timestampService: TimestampService, val repository: KeyValueRepository) :
    RateLimiterService {
    private val log = KotlinLogging.logger { }

    override suspend fun limitRequest(key: String, value: String?): Boolean {
        val lookupResult = configurationLookupService.getLimit(key, value) ?: return true
        val keyPrefix = "$key/$value"

        val result: String?
        try {
            result = runBlocking { repository.get(keyPrefix) }
        } catch (ex: KeyNotFoundException) {
            log.info { "Rate-Limiter: new target: $keyPrefix" }
            return handleResetEntry(keyPrefix, lookupResult)
        }

        val limitWindowStart = result.split("/")[0]
        val diff = timestampService.getDiffWithCurrentTime(limitWindowStart, lookupResult.timeUnit)

        // Time limit elapsed
        if (diff >= 1) {
            return handleResetEntry(keyPrefix, lookupResult)
        }

        val requests = result.split("/")[1].toLong() + 1

        if (requests > lookupResult.limit) {
            log.info { "Rate-Limiter: rejected $keyPrefix" }
            return false
        } else {
            runBlocking { repository.set(keyPrefix, "$limitWindowStart/$requests") }
            log.info { "Rate-Limiter: accepted $keyPrefix, total requests: $requests" }
            return true
        }
    }

    private fun handleResetEntry(prefix: String, limit: DescriptorLimit): Boolean {
        if (limit.limit == 0L) {
            log.info { "Rate-Limiter: rejected $prefix, total requests: 0 (Zero-limit policy on descriptor)" }
            return false
        }

        val currentTimeStamp = timestampService.getCurrentTime(limit.timeUnit)
        val requests = 1
        runBlocking { repository.set(prefix, "$currentTimeStamp/$requests") }

        log.info { "Rate-Limiter: accepted $prefix, total requests: $requests" }
        return true
    }
}
