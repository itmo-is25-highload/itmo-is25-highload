package ru.itmo.storage.storage.lsm.sstable

interface SSTableLoader {
    fun loadTablesSortedByCreationTimeDesc(): List<SSTable>
}
