package ru.itmo.storage.storage.lsm.core.wal.entry.upsert

import kotlinx.serialization.Serializable
import ru.itmo.storage.storage.lsm.core.wal.entry.OperationStatus
import ru.itmo.storage.storage.lsm.core.wal.entry.WalEntryData

@Serializable
data class UpsertWalEntryData(
    val status: OperationStatus,
    val key: String,
    val value: String,
) : WalEntryData()
