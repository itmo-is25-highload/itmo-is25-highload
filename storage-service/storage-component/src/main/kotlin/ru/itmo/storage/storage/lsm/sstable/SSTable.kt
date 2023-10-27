package ru.itmo.storage.storage.lsm.sstable

import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.bloomfilter.BloomFilter

class SSTable(
    val id: String,
    val index: AVLTree,
    val bloomFilter: BloomFilter,
) {
    fun doesNotContainKey(key: String): Boolean {
        return bloomFilter.isNotPresent(key)
    }

    fun markKeyAccessible(key: String) {
        bloomFilter.add(key)
    }
}
