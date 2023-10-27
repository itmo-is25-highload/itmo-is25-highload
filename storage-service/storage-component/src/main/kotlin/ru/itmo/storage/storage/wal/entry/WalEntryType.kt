package ru.itmo.storage.storage.wal.entry

enum class WalEntryType {
    UPSERT,
    SSTABLE_FLUSH,
    UNKNOWN,
}
