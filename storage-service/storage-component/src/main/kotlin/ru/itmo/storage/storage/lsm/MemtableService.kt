package ru.itmo.storage.storage.lsm

import ru.itmo.storage.storage.lsm.avl.AVLTree
import java.util.UUID

interface MemtableService {
    fun flushMemtableToDisk(memtable: AVLTree): UUID

    fun loadIndex(tableId: String): AVLTree

    fun loadBlockByKey(memtable: AVLTree, tableId: String, blockKey: String): List<Pair<String, String>>
}
