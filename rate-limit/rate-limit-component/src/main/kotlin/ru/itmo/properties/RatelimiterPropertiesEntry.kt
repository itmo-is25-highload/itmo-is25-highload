package ru.itmo.properties

data class RatelimiterPropertiesEntry(
    val key: String,
    val value: String?,
    val unit: RateLimitTimeUnit,
    val requests_per_unit: Long,
)
