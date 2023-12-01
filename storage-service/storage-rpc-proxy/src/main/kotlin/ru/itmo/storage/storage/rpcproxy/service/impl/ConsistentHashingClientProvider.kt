package ru.itmo.storage.storage.rpcproxy.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.storage.client.StorageClient
import ru.itmo.storage.storage.core.utils.SearchUtils
import ru.itmo.storage.storage.rpcproxy.service.ClientProvider

@Service
class ConsistentHashingClientProvider(
    entries: Collection<ConsistentHashingEntry>,
) : ClientProvider {
    private val sortedEntries: List<ConsistentHashingEntry> = entries.sortedBy { it.rangeEnd }
    private val log = KotlinLogging.logger { }
    override fun provide(hash: Long): StorageClient {
        return kostylLeftBinSearch(sortedEntries, hash).let {
            log.info { "[CLIENT-PROVIDER] Got client with rangeEnd = ${it.rangeEnd}" }
            it.client
        }
    }
}

data class ConsistentHashingEntry(
    val rangeEnd: Long,
    val client: StorageClient,
)

private fun kostylLeftBinSearch(sortedEntries: List<ConsistentHashingEntry>, hash: Long): ConsistentHashingEntry {
    val index = SearchUtils.rightBinSearch(sortedEntries, hash) { e, k -> e.rangeEnd <= k }

    return when (index) {
        -1 -> sortedEntries.first()
        sortedEntries.size - 1 -> sortedEntries.first()
        else -> sortedEntries[index + 1]
    }
}
