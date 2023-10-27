package ru.itmo.storage.storage.wal.repository

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.wal.entry.OperationStatus
import ru.itmo.storage.storage.wal.entry.WalEntryType
import ru.itmo.storage.storage.wal.entry.flush.SSTableFlushWalEntryData
import ru.itmo.storage.storage.wal.entry.toWalEntryTypeOrUnknown
import ru.itmo.storage.storage.wal.entry.upsert.UpsertWalEntryData
import java.io.File

const val WAL_FILE_PATH = "logs/wal.log"

private val log = KotlinLogging.logger { }

@Repository
class WalLogReadRepository {
    fun getNonFlushedEntries(): List<AVLTree.Entry> {
        val result = HashMap<String, String>()
        val lines = File(WAL_FILE_PATH).readLines()
        log.info { File(WAL_FILE_PATH).absolutePath }

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
                        result[data.key] = data.value
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
