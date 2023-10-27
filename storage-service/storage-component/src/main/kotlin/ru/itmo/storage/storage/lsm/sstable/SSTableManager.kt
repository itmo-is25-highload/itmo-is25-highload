package ru.itmo.storage.storage.lsm.sstable

import ru.itmo.storage.storage.lsm.avl.AVLTree

interface SSTableManager {
    fun findByKey(key: String): String?
    suspend fun saveMemtable(memtable: AVLTree)
//    fun getTables(): Deque<SSTable>
}
