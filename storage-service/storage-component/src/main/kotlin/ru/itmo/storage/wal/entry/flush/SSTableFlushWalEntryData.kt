package ru.itmo.storage.wal.entry.flush

import kotlinx.serialization.Serializable
import ru.itmo.storage.wal.entry.OperationStatus
import ru.itmo.storage.wal.entry.WalEntryData

@Serializable
data class SSTableFlushWalEntryData(
    val status: OperationStatus,
    val tableId: String? = null,
) : WalEntryData()
