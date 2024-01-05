package ru.itmo.ratelimit.component.services

import org.springframework.stereotype.Service
import ru.itmo.ratelimit.component.properties.RateLimitTimeUnit

// Завязка на лонги
@Service
interface TimestampService {
    fun getDiffWithCurrentTime(timeStamp: String, timeUnit: RateLimitTimeUnit): Long

    fun getCurrentTime(timeUnit: RateLimitTimeUnit): Long
}
