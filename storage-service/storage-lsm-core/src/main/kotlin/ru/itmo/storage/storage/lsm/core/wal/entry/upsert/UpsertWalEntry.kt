package ru.itmo.storage.storage.lsm.core.wal.entry.upsert

import ru.itmo.storage.storage.lsm.core.wal.entry.WalEntry
import ru.itmo.storage.storage.lsm.core.wal.entry.WalEntryType
import java.time.Instant

class UpsertWalEntry(
    timestamp: Instant,
    data: UpsertWalEntryData,
) : WalEntry(timestamp, WalEntryType.UPSERT, data)
