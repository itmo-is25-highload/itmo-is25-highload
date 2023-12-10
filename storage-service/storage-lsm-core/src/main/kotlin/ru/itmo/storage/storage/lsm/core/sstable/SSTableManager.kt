package ru.itmo.storage.storage.lsm.core.sstable

import ru.itmo.storage.storage.lsm.core.AVLTree
import java.util.Deque

interface SSTableManager {

    fun getCurrentSSTables(): Deque<SSTable>
    fun findByKey(key: String): String?
    suspend fun saveMemtable(memtable: AVLTree)
    fun reload()
//    fun getTables(): Deque<SSTable>
}
