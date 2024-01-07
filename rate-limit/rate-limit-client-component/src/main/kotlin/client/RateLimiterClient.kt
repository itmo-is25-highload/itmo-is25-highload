package ru.itmo.ratelimit.client

import ru.itmo.contracts.ratelimit.RateLimiterResponse

interface RateLimiterClient {
    fun checkLimit(key: String, value: String?): RateLimiterResponse

    fun incrementLimit(key: String, value: String?): Any
}
