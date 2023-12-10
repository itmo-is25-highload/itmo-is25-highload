package ru.itmo.storage.storage.lsm.core.wal.entry.flush

import ru.itmo.storage.storage.lsm.core.wal.entry.WalEntry
import ru.itmo.storage.storage.lsm.core.wal.entry.WalEntryType
import java.time.Instant

class SSTableFlushWalEntry(
    timestamp: Instant,
    data: SSTableFlushWalEntryData,
) : WalEntry(timestamp, WalEntryType.SSTABLE_FLUSH, data)
