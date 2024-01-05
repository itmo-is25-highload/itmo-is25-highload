package ru.itmo.ratelimit.client

interface RateLimiterClient {
    fun limit(key: String, value: String?): Boolean
}