package ru.itmo.storage.storage.lsm.core.wal.entry.flush

import kotlinx.serialization.Serializable
import ru.itmo.storage.storage.lsm.core.wal.entry.OperationStatus
import ru.itmo.storage.storage.lsm.core.wal.entry.WalEntryData

@Serializable
data class SSTableFlushWalEntryData(
    val status: OperationStatus,
    val tableId: String? = null,
) : WalEntryData()
