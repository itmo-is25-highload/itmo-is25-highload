package ru.itmo.storage.storage.lsm

import ru.itmo.storage.storage.lsm.avl.AVLTree

interface MemtableService {
    fun flush(memtable: AVLTree)

    fun load(tableId: String): AVLTree

    fun loadBlock(memtable: AVLTree, tableId: String, blockKey: String): List<Pair<String, String>>
}
