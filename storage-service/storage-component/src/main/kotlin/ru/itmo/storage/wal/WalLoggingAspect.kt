package ru.itmo.storage.wal

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.kotlin.logger
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import ru.itmo.storage.wal.entry.OperationStatus
import ru.itmo.storage.wal.entry.WalEntry
import ru.itmo.storage.wal.entry.upsert.UpsertWalEntry
import ru.itmo.storage.wal.entry.upsert.UpsertWalEntryData
import java.time.Instant

@Aspect
@Component
class WalLoggingAspect {
    val log = logger()

    @Pointcut("within(ru.itmo.storage.storage.KeyValueRepository+)")
    protected fun dbOperationsPointcut() {
    }

    @Pointcut("dbOperationsPointcut() && execution(* set(..))")
    protected fun upsertPointcut() {
    }

    @Before("upsertPointcut() && args(key,value,..)", argNames = "key,value")
    fun logBeforeUpsert(key: String, value: String) {
        val data = UpsertWalEntryData(OperationStatus.PENDING, key, value)
        val walEntry = UpsertWalEntry(Instant.now(), data)
        logWalEntry(walEntry)
    }

    @AfterReturning("upsertPointcut() && args(key,value,..)", argNames = "key,value")
    fun logAfterSuccessfulUpsert(key: String, value: String) {
        val data = UpsertWalEntryData(OperationStatus.SUCCESS, key, value)
        val walEntry = UpsertWalEntry(Instant.now(), data)
        logWalEntry(walEntry)
    }

    @AfterThrowing("upsertPointcut() && args(key,value,..)", argNames = "key,value")
    fun logAfterFailedUpsert(key: String, value: String) {
        val data = UpsertWalEntryData(OperationStatus.FAIL, key, value)
        val walEntry = UpsertWalEntry(Instant.now(), data)
        logWalEntry(walEntry)
    }

    private fun logWalEntry(walEntry: WalEntry) {
        log.info("${walEntry.timestamp.toEpochMilli()};${walEntry.type};${Json.encodeToString(walEntry.data)}")
    }
}
