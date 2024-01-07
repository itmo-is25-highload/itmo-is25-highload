package ru.itmo.ratelimit.component.services

import org.springframework.stereotype.Service
import ru.itmo.ratelimit.component.properties.RateLimitTimeUnit

@Service
class TimestampServiceImpl : TimestampService {
    /*
    @return diff(currentTime - timeStamp) in format of timeUnit
    */
    override fun getDiffWithCurrentTime(timeStamp: String, timeUnit: RateLimitTimeUnit): Long {
        return convertMillisToGivenTimeUnits(System.currentTimeMillis(), timeUnit) - timeStamp.toLong()
    }

    override fun getNextLimitReset(
        timeStamp: String,
        timeStampTimeUnit: RateLimitTimeUnit,
        resultTimeUnit: RateLimitTimeUnit,
    ): Long {
        val timeDiff = getDiffWithCurrentTime(timeStamp, timeStampTimeUnit)
        val diffInMillis = convertTimeStampToMillis(timeDiff, timeStampTimeUnit)
        return convertMillisToGivenTimeUnits(diffInMillis, resultTimeUnit)
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

    private fun convertTimeStampToMillis(timeStamp: Long, timeUnit: RateLimitTimeUnit) : Long {
        return when (timeUnit){
            RateLimitTimeUnit.Millisecond -> timeStamp
            RateLimitTimeUnit.Second -> timeStamp * 1000
            RateLimitTimeUnit.Minute -> convertTimeStampToMillis(timeStamp, RateLimitTimeUnit.Second) * 60
            RateLimitTimeUnit.Hour -> convertTimeStampToMillis(timeStamp, RateLimitTimeUnit.Minute) * 60
            RateLimitTimeUnit.Day -> convertTimeStampToMillis(timeStamp, RateLimitTimeUnit.Hour) * 24
        }
    }
}
