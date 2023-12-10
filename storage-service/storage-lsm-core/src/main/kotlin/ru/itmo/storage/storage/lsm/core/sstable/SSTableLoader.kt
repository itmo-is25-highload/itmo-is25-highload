package ru.itmo.storage.storage.lsm.core.sstable

interface SSTableLoader {
    fun loadTablesSortedByCreationTimeDesc(): List<SSTable>
}
