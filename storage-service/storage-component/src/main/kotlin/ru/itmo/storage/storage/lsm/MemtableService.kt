package ru.itmo.storage.storage.lsm

import ru.itmo.storage.storage.lsm.avl.AVLTree
import java.io.BufferedOutputStream
import java.util.UUID

interface MemtableService {
    fun flushMemtableToDisk(memtable: AVLTree): UUID

    fun appendBlockToSSTable(memtable: AVLTree, tableWriter: BufferedOutputStream, indexWriter: BufferedOutputStream)

    fun createEmptySSTable(): UUID

    fun loadIndex(tableId: String): List<AVLTree.Entry>

    fun loadBlockByKey(memtable: List<AVLTree.Entry>, tableId: String, blockKey: String): List<AVLTree.Entry>
}
