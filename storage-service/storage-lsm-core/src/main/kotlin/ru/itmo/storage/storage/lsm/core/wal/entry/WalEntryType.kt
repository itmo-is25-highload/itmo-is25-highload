package ru.itmo.storage.storage.lsm.core.wal.entry

enum class WalEntryType {
    UPSERT,
    SSTABLE_FLUSH,
    UNKNOWN,
}

fun String.toWalEntryTypeOrUnknown(): WalEntryType = runCatching {
    WalEntryType.valueOf(this)
}.getOrElse { WalEntryType.UNKNOWN }
