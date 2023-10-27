package ru.itmo.storage.storage.lsm.sstable

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.bloomfilter.BloomFilter
import ru.itmo.storage.storage.lsm.properties.BloomFilterProperties
import ru.itmo.storage.utils.SearchUtils
import java.util.ArrayDeque
import java.util.Deque

@Service
class SSTableManagerImpl(
    private val bloomFilterProperties: BloomFilterProperties,
    private val memtableService: MemtableService,
    loader: SSTableLoader,
) : SSTableManager {
    // Deque is used in order to extract two oldest tables and insert a new one easily when merging
    // Is sorted by creation time DESC (newest come first) -> no need to copy and reverse when iterating over it
    private lateinit var ssTables: Deque<SSTable>

    init {
        this.ssTables = loader.loadTablesSortedByCreationTimeDesc()
            .toCollection(ArrayDeque())
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

    override fun saveMemtable(memtable: AVLTree) {
        runBlocking {
            launch {
                // TODO either make this call async or fill the filter while inserting to the memtable
                val bloomFilter = getBloomFilter(memtable)
                val id: String = memtableService.flushMemtableToDisk(memtable).toString()
                val index: AVLTree = memtableService.loadIndex(id)
                ssTables.addFirst(SSTable(id, index, bloomFilter))
            }
        }
    }

    private fun findByKeyInTable(table: SSTable, key: String): String? {
        if (table.doesNotContainKey(key)) {
            return null
        }

        val orderedEntries = table.index.orderedEntries()
        val blockIndex = SearchUtils.rightBinSearch(orderedEntries, key) { e, k -> e.key <= k }
        if (!isBlockInTable(blockIndex, orderedEntries)) {
            return null
        }
        val pairs = memtableService.loadBlockByKey(table.index, table.id, orderedEntries[blockIndex].key)
        val valueIndex = pairs.binarySearchBy(key) { it.first }
        if (isValueInBlock(valueIndex)) {
            table.markKeyAccessible(key)
            return pairs[valueIndex].second
        }
        return null
    }

    private fun isBlockInTable(
        leftIdx: Int,
        orderedEntries: List<AVLTree.Entry>,
    ) = leftIdx >= 0 && (orderedEntries.size == 1 || leftIdx < orderedEntries.size - 1)

    private fun isValueInBlock(valueIndex: Int) = valueIndex >= 0

    private fun getBloomFilter(memtable: AVLTree): BloomFilter {
        val entries = memtable.orderedEntries()
        val filter = BloomFilter(bloomFilterProperties.maxSize, entries.size)
        for (entry in memtable.orderedEntries()) {
            filter.add(entry.key)
        }

        return filter
    }
}
