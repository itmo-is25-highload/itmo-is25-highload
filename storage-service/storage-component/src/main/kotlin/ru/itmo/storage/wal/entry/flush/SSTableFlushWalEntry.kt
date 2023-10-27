package ru.itmo.storage.wal.entry.flush

import ru.itmo.storage.wal.entry.WalEntry
import ru.itmo.storage.wal.entry.WalEntryType
import java.time.Instant

class SSTableFlushWalEntry(
    timestamp: Instant,
    data: SSTableFlushWalEntryData,
) : WalEntry(timestamp, WalEntryType.SSTABLE_FLUSH, data)
