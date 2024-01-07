package ru.itmo.contracts.ratelimit

data class RateLimitIncrementRequest(
    val key: String,
    val value: String?
)