package ru.itmo.ratelimit.component.properties

data class RatelimiterPropertiesEntry(
    var key: String,
    var value: String?,
    var unit: RateLimitTimeUnit,
    var requests_per_unit: Long,
)
