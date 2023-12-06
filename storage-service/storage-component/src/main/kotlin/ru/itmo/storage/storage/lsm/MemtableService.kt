package ru.itmo.storage.storage.lsm

import ru.itmo.storage.storage.lsm.avl.AVLTree
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.nio.file.Path

interface MemtableService {
    fun flushMemtableToDisk(memtable: AVLTree): String

    fun appendBlockToSSTable(
        memtable: AVLTree,
        tableWriter: BufferedOutputStream,
        indexWriter: BufferedOutputStream,
        table: Path,
        tableId: String
    )

    fun createEmptySSTable(): String

    fun loadIndex(tableId: String): List<AVLTree.Entry>

    fun loadBlockByKey(memtable: List<AVLTree.Entry>, tableId: String, blockKey: String): List<AVLTree.Entry>
}
