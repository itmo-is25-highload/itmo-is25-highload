package ru.itmo.storage.wal.entry

import kotlinx.serialization.Serializable
import ru.itmo.storage.wal.serialization.WalEntryDataSerializer

@Serializable(with = WalEntryDataSerializer::class)
abstract class WalEntryData
