package ru.itmo.storage.storage.lsm.core.wal.repository

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.lsm.core.AVLTree
import ru.itmo.storage.storage.lsm.core.wal.entry.OperationStatus
import ru.itmo.storage.storage.lsm.core.wal.entry.WalEntryType
import ru.itmo.storage.storage.lsm.core.wal.entry.flush.SSTableFlushWalEntryData
import ru.itmo.storage.storage.lsm.core.wal.entry.toWalEntryTypeOrUnknown
import ru.itmo.storage.storage.lsm.core.wal.entry.upsert.UpsertWalEntryData
import java.io.File

private val log = KotlinLogging.logger { }

@Repository
class WalLogReadRepository {

    @Value("\${logs.dir:logs/wal.log}")
    private lateinit var walPath: String

    fun getNonFlushedEntries(): List<AVLTree.Entry> {
        val result = HashMap<String, String>()
        val lines = File(walPath).readLines()
        log.info { File(walPath).absolutePath }

        for (i in lines.size - 1 downTo 0) {
            val line = lines[i]

            val (_, type, jsonString) = line.split(";")

            val shouldBreak = when (type.toWalEntryTypeOrUnknown()) {
                WalEntryType.SSTABLE_FLUSH -> {
                    val data = Json.decodeFromString<SSTableFlushWalEntryData>(jsonString)

                    data.status == OperationStatus.SUCCESS
                }

                WalEntryType.UPSERT -> {
                    val data = Json.decodeFromString<UpsertWalEntryData>(jsonString)

                    if (data.status == OperationStatus.SUCCESS) {
                        result.putIfAbsent(data.key, data.value)
                    }

                    false
                }

                WalEntryType.UNKNOWN -> {
                    false
                }
            }

            if (shouldBreak) {
                break
            }
        }

        return result.entries.map { (key, value) -> AVLTree.Entry(key, value) }
    }
}
