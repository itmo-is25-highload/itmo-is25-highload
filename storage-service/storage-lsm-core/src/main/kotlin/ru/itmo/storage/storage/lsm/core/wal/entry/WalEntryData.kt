package ru.itmo.storage.storage.lsm.core.wal.entry

import kotlinx.serialization.Serializable
import ru.itmo.storage.storage.lsm.core.wal.serialization.WalEntryDataSerializer

@Serializable(with = WalEntryDataSerializer::class)
abstract class WalEntryData
