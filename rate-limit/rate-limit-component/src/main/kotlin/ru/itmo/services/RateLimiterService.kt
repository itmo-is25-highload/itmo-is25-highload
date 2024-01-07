package ru.itmo.ratelimit.component.services

import org.springframework.stereotype.Service
import ru.itmo.contracts.ratelimit.RateLimiterResponse

@Service
interface RateLimiterService {
    suspend fun checkLimit(key: String, value: String?): RateLimiterResponse

    suspend fun incrementRequests(key: String, value: String?)
}
