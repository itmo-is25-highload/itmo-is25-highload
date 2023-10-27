package ru.itmo.storage.wal.entry.upsert

import ru.itmo.storage.wal.entry.WalEntry
import ru.itmo.storage.wal.entry.WalEntryType
import java.time.Instant

class UpsertWalEntry(
    timestamp: Instant,
    data: UpsertWalEntryData,
) : WalEntry(timestamp, WalEntryType.UPSERT, data)
