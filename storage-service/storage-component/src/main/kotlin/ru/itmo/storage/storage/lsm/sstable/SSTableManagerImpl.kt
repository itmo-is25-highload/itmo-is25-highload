package ru.itmo.storage.storage.lsm.sstable

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.itmo.storage.storage.config.MEMTABLE_FLUSH
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.bloomfilter.BloomFilter
import ru.itmo.storage.storage.lsm.properties.BloomFilterProperties
import ru.itmo.storage.storage.utils.SearchUtils
import java.util.*

@Service
class SSTableManagerImpl(
    private val bloomFilterProperties: BloomFilterProperties,
    private val memtableService: MemtableService,
    @Qualifier(MEMTABLE_FLUSH) private val receiveChannel: MutableSharedFlow<AVLTree>,
    loader: SSTableLoader,
) : SSTableManager {
    // Deque is used in order to extract two oldest tables and insert a new one easily when merging
    // Is sorted by creation time DESC (newest come first) -> no need to copy and reverse when iterating over it
    private lateinit var ssTables: Deque<SSTable>
    private val log = KotlinLogging.logger { }

    init {
        this.ssTables = loader.loadTablesSortedByCreationTimeDesc()
            .toCollection(ArrayDeque())

        log.info { "Collected ${ssTables.joinToString(separator = ", ") { it.id }}" }
    }

    @PostConstruct
    fun receive() {
        CoroutineScope(SupervisorJob() + defaultExceptionHandler()).launch {
            log.info("Start consuming")
            receiveChannel.collect {
                saveMemtable(it)
            }
        }
    }

    override fun getCurrentSSTables(): Deque<SSTable> {
        return ssTables
    }

    override fun findByKey(key: String): String? {
        for (table in ssTables) {
            val value = findByKeyInTable(table, key)
            if (value != null) {
                return value
            }
        }

        return null
    }

    override suspend fun saveMemtable(memtable: AVLTree) {
        coroutineScope {
            log.info { "Start saveMemtable with $memtable" }
            val bloomFilter = async { getBloomFilter(memtable) }

            log.info { "Flush memtable to disk with $memtable" }
            val id: String = memtableService.flushMemtableToDisk(memtable)
            val index: List<AVLTree.Entry> = memtableService.loadIndex(id)

            log.info { "Add ssTable to list with $memtable" }
            ssTables.addFirst(SSTable(id, index, bloomFilter.await()))
        }
    }

    private fun findByKeyInTable(table: SSTable, key: String): String? {
        if (table.doesNotContainKey(key)) {
            log.info { "Bloom filter said that key isn't present in table ${table.id}" }
        }

        val orderedEntries = table.index
        log.info { "Searching for key $key in entries $orderedEntries in table ${table.id}" }

        val blockIndex = SearchUtils.rightBinSearch(orderedEntries, key) { e, k -> e.key <= k }
        if (!isBlockInTable(blockIndex, orderedEntries)) {
            log.info { "Block is not in table ${table.id}" }
            return null
        }
        val pairs = memtableService.loadBlockByKey(table.index, table.id, orderedEntries[blockIndex].key)
        val valueIndex = pairs.binarySearchBy(key) { it.key }
        if (isValueInBlock(valueIndex)) {
            table.markKeyAccessible(key)
            return pairs[valueIndex].value
        }
        return null
    }

    private fun isBlockInTable(
        leftIdx: Int,
        orderedEntries: List<AVLTree.Entry>,
    ): Boolean {
        log.info { "Is block in table leftIndex $leftIdx and ${orderedEntries.size}" }

        return leftIdx >= 0 && (orderedEntries.size == 1 || leftIdx <= orderedEntries.size - 1)
    }

    private fun isValueInBlock(valueIndex: Int) = valueIndex >= 0

    private fun getBloomFilter(memtable: AVLTree): BloomFilter {
        log.info { "Get Bloom filter for $memtable" }
        val entries = memtable.orderedEntries()
        val filter = BloomFilter(bloomFilterProperties.maxSize, entries.size)
        for (entry in memtable.orderedEntries()) {
            filter.add(entry.key)
        }

        return filter
    }

    fun defaultExceptionHandler() = CoroutineExceptionHandler { _, exception ->
        log.info { "CoroutineExceptionHandler got $exception" }
        log.info { exception.stackTraceToString() }
    }
}
