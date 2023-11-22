package ru.itmo.storage.storage.identifiers

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.time.Clock
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

private const val RETRY_COUNT = 1000

@Service
class TimestampIdentifierService(
    private val clock: Clock,
) : UniqueIdentifierService {

    private val count = AtomicInteger()
    private val log = KotlinLogging.logger { }

    @PostConstruct
    fun resetCount() {
        CoroutineScope(SupervisorJob() + defaultExceptionHandler()).launch {
            while (isActive) {
                delay(1000)
                count.set(0)
            }
        }
    }

    override fun nextIdentifier(): String {
        repeat(RETRY_COUNT) {
            val epochSeconds = Instant.now(clock).epochSecond
            val number = count.get()

            if (count.compareAndSet(number, number + 10)) {
                return "${epochSeconds}${number.toString().padStart(4, '0')}"
            }
        }

        throw IllegalStateException("Retry count for unique identifier exceeded")
    }

    fun defaultExceptionHandler() = CoroutineExceptionHandler { _, exception ->
        log.info { "CoroutineExceptionHandler got $exception" }
        log.info { exception.stackTraceToString() }
    }
}
