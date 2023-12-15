package ru.itmo.entries

import ru.itmo.properties.RateLimitTimeUnit

data class DescriptorLimit(
    val limit: Long,
    val timeUnit: RateLimitTimeUnit
)