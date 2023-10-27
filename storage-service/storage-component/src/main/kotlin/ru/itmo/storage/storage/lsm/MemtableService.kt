package ru.itmo.storage.storage.lsm

import ru.itmo.storage.storage.lsm.avl.AVLTree
import java.util.UUID

interface MemtableService {
    fun flushMemtableToDisk(memtable: AVLTree): UUID

    fun loadIndex(tableId: String): List<AVLTree.Entry>

    fun loadBlockByKey(memtable: List<AVLTree.Entry>, tableId: String, blockKey: String): List<Pair<String, String>>
}
