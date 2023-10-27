package ru.itmo.storage.storage.lsm

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.KeyValueRepository
import ru.itmo.storage.storage.config.MEMTABLE_FLUSH
import ru.itmo.storage.storage.exception.KeyNotFoundException
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree
import ru.itmo.storage.storage.lsm.properties.LsmTreeRepositoryProperties
import ru.itmo.storage.storage.lsm.sstable.SSTableManager

@Repository
class LsmTreeKeyValueRepository(
    private val ssTableManager: SSTableManager,
    private val properties: LsmTreeRepositoryProperties,
    @Qualifier(MEMTABLE_FLUSH) private val flushChannel: MutableSharedFlow<AVLTree>,
) : KeyValueRepository {

    private var memTable: AVLTree = DefaultAVLTree()

    private val log = KotlinLogging.logger { }

    override suspend fun get(key: String): String {
        return memTable.find(key)
            ?: ssTableManager.findByKey(key)
            ?: throw KeyNotFoundException(key)
    }

    override suspend fun set(key: String, value: String) {
        if (memTable.sizeInBytes >= properties.maxSize) {
            log.info { "Flush memtable to disk $memTable" }
            flushChannel.emit(memTable)

            log.info { "Reset AVLTree" }
            memTable = DefaultAVLTree()
        }

        memTable.upsert(key, value)
    }
}
