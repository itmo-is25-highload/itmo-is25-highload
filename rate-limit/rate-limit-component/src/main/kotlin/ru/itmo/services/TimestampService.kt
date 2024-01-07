package ru.itmo.ratelimit.component.services

import org.springframework.stereotype.Service
import ru.itmo.ratelimit.component.properties.RateLimitTimeUnit

// Завязка на лонги
@Service
interface TimestampService {
    fun getDiffWithCurrentTime(timeStamp: String, timeUnit: RateLimitTimeUnit): Long

    fun getNextLimitReset(
        timeStamp: String,
        timeStampTimeUnit: RateLimitTimeUnit,
        resultTimeUnit: RateLimitTimeUnit = RateLimitTimeUnit.Second,
    ): Long

    fun getCurrentTime(timeUnit: RateLimitTimeUnit): Long
}
