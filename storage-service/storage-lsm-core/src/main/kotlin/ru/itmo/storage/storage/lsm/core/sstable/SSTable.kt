package ru.itmo.storage.storage.lsm.core.sstable

import ru.itmo.storage.storage.lsm.core.AVLTree
import ru.itmo.storage.storage.lsm.core.bloomfilter.BloomFilter

class SSTable(
    val id: String,
    val index: List<AVLTree.Entry>,
    val bloomFilter: BloomFilter,
) {
    fun doesNotContainKey(key: String): Boolean {
        return bloomFilter.isNotPresent(key)
    }

    fun markKeyAccessible(key: String) {
        bloomFilter.add(key)
    }
}
