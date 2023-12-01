package ru.itmo.storage.storage.wal

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.kotlin.logger
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import ru.itmo.storage.storage.wal.entry.OperationStatus
import ru.itmo.storage.storage.wal.entry.WalEntry
import ru.itmo.storage.storage.wal.entry.flush.SSTableFlushWalEntry
import ru.itmo.storage.storage.wal.entry.flush.SSTableFlushWalEntryData
import ru.itmo.storage.storage.wal.entry.upsert.UpsertWalEntry
import ru.itmo.storage.storage.wal.entry.upsert.UpsertWalEntryData
import java.time.Instant
import java.util.*

@Aspect
@Component
class WalLoggingAspect {
    val log = logger()

    @Pointcut("within(ru.itmo.storage.storage.core.KeyValueRepository+)")
    protected fun dbOperationsPointcut() {
    }

    @Pointcut("dbOperationsPointcut() && execution(* set(..))")
    protected fun upsertPointcut() {
    }

    @Pointcut("within(ru.itmo.storage.storage.lsm.MemtableService+)")
    protected fun memtableServicePointcut() {
    }

    @Pointcut("memtableServicePointcut() && execution(* flushMemtableToDisk(..))")
    protected fun ssTableFlushingPointcut() {
    }

    @Before("upsertPointcut() && args(key,value,..)", argNames = "key,value")
    fun logBeforeUpsert(key: String, value: String) {
        val data = UpsertWalEntryData(OperationStatus.PENDING, key, value)
        val walEntry = UpsertWalEntry(getCurrentTime(), data)
        logWalEntry(walEntry)
    }

    @AfterReturning("upsertPointcut() && args(key,value,..)", argNames = "key,value")
    fun logAfterSuccessfulUpsert(key: String, value: String) {
        val data = UpsertWalEntryData(OperationStatus.SUCCESS, key, value)
        val walEntry = UpsertWalEntry(getCurrentTime(), data)
        logWalEntry(walEntry)
    }

    @AfterThrowing("upsertPointcut() && args(key,value,..)", argNames = "key,value")
    fun logAfterFailedUpsert(key: String, value: String) {
        val data = UpsertWalEntryData(OperationStatus.FAIL, key, value)
        val walEntry = UpsertWalEntry(getCurrentTime(), data)
        logWalEntry(walEntry)
    }

    @Before("ssTableFlushingPointcut()")
    fun logBeforeFlushingSSTable() {
        val data = SSTableFlushWalEntryData(OperationStatus.PENDING)
        val walEntry = SSTableFlushWalEntry(getCurrentTime(), data)
        logWalEntry(walEntry)
    }

    @AfterReturning("ssTableFlushingPointcut()", returning = "tableId", argNames = "tableId")
    fun logAfterSuccessfulFlushingSSTable(tableId: String) {
        val data = SSTableFlushWalEntryData(OperationStatus.SUCCESS, tableId)
        val walEntry = SSTableFlushWalEntry(getCurrentTime(), data)
        logWalEntry(walEntry)
    }

    @AfterThrowing("ssTableFlushingPointcut()")
    fun logAfterFailedFlushingSSTable() {
        val data = SSTableFlushWalEntryData(OperationStatus.FAIL)
        val walEntry = SSTableFlushWalEntry(getCurrentTime(), data)
        logWalEntry(walEntry)
    }

    private fun logWalEntry(walEntry: WalEntry) {
        log.info("${walEntry.timestamp.toEpochMilli()};${walEntry.type};${Json.encodeToString(walEntry.data)}")
    }

    private fun getCurrentTime(): Instant = Instant.now()
}
