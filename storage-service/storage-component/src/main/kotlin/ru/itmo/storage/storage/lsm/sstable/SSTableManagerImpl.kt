package ru.itmo.storage.storage.lsm.sstable

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import java.util.ArrayDeque
import java.util.Deque

@Service
class SSTableManagerImpl(
    private val memtableService: MemtableService,
    loader: SSTableLoader,
) : SSTableManager {
    // Deque is used in order to extract two oldest tables and insert a new one easily when merging
    // Is sorted by creation time (oldest come first)
    private var ssTables: Deque<SSTable> = ArrayDeque()

    init {
        this.ssTables = loader.loadTables()
            .sortedBy { it.id }
            .let { ArrayDeque() }
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
                ssTables.addLast(SSTable(id, index))
            }
        }
    }

    private fun findByKeyInTable(table: SSTable, key: String): String? {
        if (!table.containsKey(key)) {
            return null
        }

        val blockKey = table.index.orderedEntries().firstOrNull { it.key <= key } ?: return null
        val pairs = memtableService.loadBlockByKey(table.index, table.id, blockKey.key)
        return pairs.firstOrNull { it.first == key }?.first
    }
}
