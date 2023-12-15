package ru.itmo.services.impl

import ru.itmo.config.RatelimiterConfiguration
import ru.itmo.entries.DescriptorLimit
import ru.itmo.properties.RateLimitTimeUnit
import ru.itmo.services.RateLimiterConfigurationLookupService

class RateLimiterConfigurationLookupServiceImpl
(val configuration: RatelimiterConfiguration) : RateLimiterConfigurationLookupService {
    override fun getLimit(key: String, value: String?): DescriptorLimit? {
        val properties = configuration.properties
        val entry = properties.find { x -> x.key == key && x.value == value } ?: return null

        val limit: Long = entry.requests_per_unit
        val timeUnit: RateLimitTimeUnit = entry.unit

        return DescriptorLimit(limit, timeUnit)
    }
}
