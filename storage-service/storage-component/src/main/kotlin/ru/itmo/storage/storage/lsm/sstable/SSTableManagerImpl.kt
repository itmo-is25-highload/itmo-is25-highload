package ru.itmo.storage.storage.lsm.sstable

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.utils.SearchUtils
import java.util.ArrayDeque
import java.util.Deque

@Service
class SSTableManagerImpl(
    private val memtableService: MemtableService,
    loader: SSTableLoader,
) : SSTableManager {
    // Deque is used in order to extract two oldest tables and insert a new one easily when merging
    // Is sorted by creation time DESC (newest come first) -> no need to copy and reverse when iterating over it
    private lateinit var ssTables: Deque<SSTable>

    init {
        this.ssTables = loader.loadTables()
            .sortedByDescending { it.id }
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
                val id: String = memtableService.flushMemtableToDisk(memtable).toString()
                val index: AVLTree = memtableService.loadIndex(id)
                ssTables.addFirst(SSTable(id, index))
            }
        }
    }

    private fun findByKeyInTable(table: SSTable, key: String): String? {
        if (!table.containsKey(key)) {
            return null
        }

        val orderedEntries = table.index.orderedEntries()
        val blockIndex = SearchUtils.rightBinSearch(orderedEntries, key) { e, k -> e.key <= k }
        if (isBlockInTable(blockIndex, orderedEntries)) {
            return null
        }
        val pairs = memtableService.loadBlockByKey(table.index, table.id, orderedEntries[blockIndex].key)
        val valueIndex = pairs.binarySearchBy(key) { it.first }
        return if (valueIndex >= 0) pairs[valueIndex].second else null
    }

    private fun isBlockInTable(
        leftIdx: Int,
        orderedEntries: List<AVLTree.Entry>,
    ) = leftIdx == -1 || (orderedEntries.size > 1 && leftIdx == orderedEntries.size - 1)
}
