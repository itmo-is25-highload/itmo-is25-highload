package ru.itmo.storage.storage.lsm.sstable

import ru.itmo.storage.storage.lsm.avl.AVLTree

class SSTable(
    val id: String,
    val index: AVLTree,
) {
    // TODO Check if Bloom filter optimization is possible
    fun containsKey(key: String): Boolean {
        return true
    }
}
