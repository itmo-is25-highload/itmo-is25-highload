package ru.itmo.services

import org.springframework.stereotype.Service

@Service
interface RateLimiterService {
    suspend fun limitRequest(key: String, value: String?): Boolean
}
