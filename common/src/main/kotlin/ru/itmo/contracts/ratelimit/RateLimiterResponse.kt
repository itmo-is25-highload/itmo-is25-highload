package ru.itmo.contracts.ratelimit

class RateLimiterResponse(
    var isAllowed: Boolean,
    var isLimited: Boolean,
    var requestsLeft: Long? = null,
    var secondsLeftTillReset: Long? = null,
)