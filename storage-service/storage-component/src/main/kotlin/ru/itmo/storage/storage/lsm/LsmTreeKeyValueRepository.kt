package ru.itmo.storage.storage.lsm

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository
import ru.itmo.storage.storage.core.KeyValueRepository
import ru.itmo.storage.storage.config.MEMTABLE_FLUSH
import ru.itmo.storage.storage.core.exception.KeyNotFoundException
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree
import ru.itmo.storage.storage.lsm.properties.LsmTreeRepositoryProperties
import ru.itmo.storage.storage.lsm.sstable.SSTableManager
import ru.itmo.storage.storage.wal.repository.WalLogReadRepository

@Repository
class LsmTreeKeyValueRepository(
    private val ssTableManager: SSTableManager,
    private val properties: LsmTreeRepositoryProperties,
    @Qualifier(MEMTABLE_FLUSH) private val flushChannel: MutableSharedFlow<AVLTree>,
    private val walLogReadRepository: WalLogReadRepository,
) : KeyValueRepository {

    private lateinit var memTable: AVLTree

    private val log = KotlinLogging.logger { }

    @PostConstruct
    fun initNonFlushed() {
        log.info { "Trying to restore state" }
        val table = DefaultAVLTree()
        walLogReadRepository.getNonFlushedEntries().forEach { (key, value) ->
            log.info { "Restore entry $key $value" }
            table.upsert(key, value)
        }

        memTable = table
    }

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
