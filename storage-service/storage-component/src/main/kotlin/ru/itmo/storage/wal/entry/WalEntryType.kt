package ru.itmo.storage.wal.entry

enum class WalEntryType {
    UPSERT,
    SSTABLE_FLUSH,
    UNKNOWN,
}