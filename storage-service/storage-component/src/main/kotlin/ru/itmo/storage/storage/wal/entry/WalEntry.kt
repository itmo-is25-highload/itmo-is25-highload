package ru.itmo.storage.storage.wal.entry

import java.time.Instant

abstract class WalEntry(
    val timestamp: Instant,
    val type: WalEntryType,
    val data: WalEntryData,
)