package ru.itmo.ratelimit.component.services

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.contracts.ratelimit.RateLimiterResponse
import ru.itmo.ratelimit.component.entries.DescriptorLimit
import ru.itmo.storage.storage.core.KeyValueRepository
import ru.itmo.storage.storage.core.exception.KeyNotFoundException

@Service
class RateLimiterServiceImpl(private val configurationLookupService: RateLimiterConfigurationLookupService, private val timestampService: TimestampService, val repository: KeyValueRepository) :
    RateLimiterService {
    private val log = KotlinLogging.logger { }

    override suspend fun checkLimit(key: String, value: String?): RateLimiterResponse {
        val lookupResult = configurationLookupService.getLimit(key, value) ?: return RateLimiterResponse(isAllowed = true, isLimited = false)
        val keyPrefix = "$key/$value"

        var result: String? = null
        var requests: Long = 0

        try {
            result = runBlocking { repository.get(keyPrefix) }
        } catch (ex: KeyNotFoundException) {
            log.info { "Rate-Limiter: new target: $keyPrefix" }
        }

        var limitWindowStart: String = Long.MAX_VALUE.toString()

        if (result != null) {
            limitWindowStart = result.split("/")[0]
        }

        if (result != null && !isTimeElapsed(result, lookupResult)) {
            requests = result.split("/")[1].toLong()
        }

        if (requests >= lookupResult.limit) {
            log.info { "Rate-Limiter: rejected $keyPrefix" }
            return RateLimiterResponse(
                isAllowed = false,
                isLimited = true,
                requestsLeft = 0,
                timestampService.getNextLimitReset(limitWindowStart, lookupResult.timeUnit),
            )
        } else {
            log.info { "Rate-Limiter: accepted $keyPrefix, total requests: $requests" }
            var requestsLeft: Long = lookupResult.limit - requests - 1
            if (requestsLeft < 0) {
                requestsLeft = 0
            }

            return RateLimiterResponse(
                isAllowed = true,
                isLimited = true,
                requestsLeft = requestsLeft,
                timestampService.getNextLimitReset(limitWindowStart, lookupResult.timeUnit),
            )
        }
    }

    override suspend fun incrementRequests(key: String, value: String?) {
        val lookupResult = configurationLookupService.getLimit(key, value)!!
        val keyPrefix = "$key/$value"

        var result: String
        try {
            result = runBlocking { repository.get(keyPrefix) }
        } catch (ex: KeyNotFoundException) {
            log.info { "Rate-Limiter: new target: $keyPrefix" }
            handleResetEntry(keyPrefix, lookupResult)
            return
        }

        if (isTimeElapsed(result, lookupResult)) {
            handleResetEntry(keyPrefix, lookupResult)
            return
        }

        var limitWindowStart = result.split("/")[0]
        val requests = result.split("/")[1].toLong() + 1

        log.info { "Rate-Limiter: incrementing $keyPrefix, total requests: $requests" }
        runBlocking { repository.set(keyPrefix, "$limitWindowStart/$requests") }
    }

    private fun isTimeElapsed(keyLookup: String, descriptorLookup: DescriptorLimit): Boolean {
        val limitWindowStart = keyLookup.split("/")[0]
        val diff = timestampService.getDiffWithCurrentTime(limitWindowStart, descriptorLookup.timeUnit)

        return diff >= 1
    }

    private fun handleResetEntry(prefix: String, limit: DescriptorLimit) {
        val currentTimeStamp = timestampService.getCurrentTime(limit.timeUnit)
        val requests = 1
        runBlocking { repository.set(prefix, "$currentTimeStamp/$requests") }

        log.info { "Rate-Limiter: accepted $prefix, total requests: $requests" }
    }
}
