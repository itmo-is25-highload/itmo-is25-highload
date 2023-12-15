package ru.itmo.services.impl

import ru.itmo.properties.RateLimitTimeUnit
import ru.itmo.services.TimestampService

class TimestampServiceImpl : TimestampService {
    /*
    @return diff(currentTime - timeStamp) in format of timeUnit
    */
    override fun getDiffWithCurrentTime(timeStamp: String, timeUnit: RateLimitTimeUnit): Long {
        return convertMillisToGivenTimeUnits(System.currentTimeMillis(), timeUnit) - timeStamp.toLong()
    }

    override fun getCurrentTime(timeUnit: RateLimitTimeUnit): Long {
        return convertMillisToGivenTimeUnits(System.currentTimeMillis(), timeUnit)
    }

    private fun convertMillisToGivenTimeUnits(timeStamp: Long, timeUnit: RateLimitTimeUnit): Long {
        return when (timeUnit) {
            RateLimitTimeUnit.Millisecond -> timeStamp
            RateLimitTimeUnit.Second -> timeStamp / 1000
            RateLimitTimeUnit.Minute -> convertMillisToGivenTimeUnits(timeStamp, RateLimitTimeUnit.Second) / 60
            RateLimitTimeUnit.Hour -> convertMillisToGivenTimeUnits(timeStamp, RateLimitTimeUnit.Minute) / 60
            RateLimitTimeUnit.Day -> convertMillisToGivenTimeUnits(timeStamp, RateLimitTimeUnit.Hour) / 24
        }
    }
}
