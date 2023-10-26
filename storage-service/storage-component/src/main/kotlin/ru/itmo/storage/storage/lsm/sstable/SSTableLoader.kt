package ru.itmo.storage.storage.lsm.sstable

interface SSTableLoader {
    fun loadTables(): List<SSTable>
}
