package ru.itmo.storage.wal.serialization

import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import ru.itmo.storage.wal.entry.WalEntryData

object WalEntryDataSerializer : JsonContentPolymorphicSerializer<WalEntryData>(WalEntryData::class) {
    override fun selectDeserializer(element: JsonElement) = WalEntryData.serializer()
}
