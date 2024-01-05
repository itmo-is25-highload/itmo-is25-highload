package ru.itmo.ratelimit.component.services

import org.springframework.stereotype.Service
import ru.itmo.ratelimit.RatelimiterConfiguration
import ru.itmo.ratelimit.component.entries.DescriptorLimit
import ru.itmo.ratelimit.component.properties.RateLimitTimeUnit

@Service
class RateLimiterConfigurationLookupServiceImpl(val configuration: RatelimiterConfiguration) : RateLimiterConfigurationLookupService {
    override fun getLimit(key: String, value: String?): DescriptorLimit? {
        val properties = configuration.properties
        val entry = properties.find { x -> x.key == key && x.value == value } ?: return null

        val limit: Long = entry.requests_per_unit
        val timeUnit: RateLimitTimeUnit = entry.unit

        return DescriptorLimit(limit, timeUnit)
    }
}
