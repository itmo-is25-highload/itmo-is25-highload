package ru.itmo.ratelimit.component.entries

import ru.itmo.ratelimit.component.properties.RateLimitTimeUnit

data class DescriptorLimit(
    val limit: Long,
    val timeUnit: RateLimitTimeUnit,
)
