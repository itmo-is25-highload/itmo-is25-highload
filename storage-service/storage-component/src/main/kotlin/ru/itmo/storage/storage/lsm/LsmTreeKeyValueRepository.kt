package ru.itmo.storage.storage.lsm

import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.KeyValueRepository
import ru.itmo.storage.storage.exception.KeyNotFoundException
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree
import ru.itmo.storage.storage.lsm.properties.LsmTreeRepositoryProperties
import ru.itmo.storage.storage.lsm.sstable.SSTableManager

@Repository
class LsmTreeKeyValueRepository(
    private val ssTableManager: SSTableManager,
    private val properties: LsmTreeRepositoryProperties,
) : KeyValueRepository {

    private var memTable: AVLTree = DefaultAVLTree()

    override fun get(key: String): String {
        return memTable.find(key)
            ?: ssTableManager.findByKey(key)
            ?: throw KeyNotFoundException(key)
    }

    override fun set(key: String, value: String) {
        if (memTable.sizeInBytes >= properties.maxSize) {
            ssTableManager.saveMemtable(memTable.copy())
            memTable = DefaultAVLTree()
        }
        memTable.upsert(key, value)
    }
}
