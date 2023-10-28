package ru.itmo.storage.storage.jobs.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Service
import ru.itmo.storage.storage.jobs.properties.MergeSstableJobProperties
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree
import ru.itmo.storage.storage.lsm.bloomfilter.BloomFilter
import ru.itmo.storage.storage.lsm.properties.BloomFilterProperties
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import ru.itmo.storage.storage.lsm.sstable.SSTable
import ru.itmo.storage.storage.lsm.sstable.SSTableManager
import java.io.BufferedOutputStream
import java.nio.file.Path
import kotlin.io.path.outputStream

@DisallowConcurrentExecution
@Service
class MergeSsTableJob(
    private val mergeSstableJobProperties: MergeSstableJobProperties,
    private val memtableService: MemtableService,
    private val ssTableManager: SSTableManager,
    private val flushProperties: LsmRepositoryFlushProperties,
    private val bloomFilterProperties: BloomFilterProperties,
) : QuartzJobBean() {

    private val log = KotlinLogging.logger { }

    // CANCER
    override fun executeInternal(context: JobExecutionContext) {
        log.info { "Job execution started" }
        val tables = ssTableManager.getCurrentSSTables()
        val firstTable = tables.pollFirst()
        val secondTable = tables.pollFirst()

        if (firstTable == null || secondTable == null) {
            log.info { "Not enough SSTables to merge" }
            if (firstTable != null) { tables.addFirst(firstTable) }
            return
        }

        val newTable = memtableService.createEmptySSTable()
        val newTablePath = Path.of("${flushProperties.tableParentDir}/$newTable/table")
        val newTableIndexPath = Path.of("${flushProperties.tableParentDir}/$newTable/index")

        newTablePath.outputStream().buffered().use { tWriter ->
            newTableIndexPath.outputStream().buffered().use { iWriter ->
                mergeSSTables(firstTable, secondTable, tWriter, iWriter)
            }
        }

        val index = memtableService.loadIndex(newTable.toString())
        val ssTable = SSTable(newTable.toString(), index, BloomFilter(bloomFilterProperties.maxSize, index.size))
        tables.addFirst(ssTable)
    }

    private fun mergeSSTables(firstTable: SSTable, secondTable: SSTable, tableWriter: BufferedOutputStream, indexWriter: BufferedOutputStream) {
        val firstTableIndex = firstTable.index
        val secondTableIndex = secondTable.index
        var newBlock: AVLTree = DefaultAVLTree()

        var firstIndexPointer = 0
        var secondIndexPointer = 0

        var firstTableValue = firstTableIndex[firstIndexPointer++]
        var secondTableValue = secondTableIndex[secondIndexPointer++]

        var firstTableBlock = memtableService.loadBlockByKey(firstTableIndex, firstTable.id, firstTableValue.key)
        var secondTableBlock = memtableService.loadBlockByKey(secondTableIndex, secondTable.id, secondTableValue.key)

        var firstBlockPointer: IntWrapper = IntWrapper(0)
        var secondBlockPointer: IntWrapper = IntWrapper(0)

        var currentBlockSize: IntWrapper = IntWrapper(0)

        // While there are still blocks
        var merger = BlockMerger(firstTableBlock, secondTableBlock, firstBlockPointer, secondBlockPointer, currentBlockSize, newBlock, flushProperties.blockSizeInBytes, memtableService, tableWriter, indexWriter)

        while (firstIndexPointer < firstTableIndex.size && secondIndexPointer < secondTableIndex.size) {
            merger.merge()

            if (firstBlockPointer.get() == firstTableBlock.size) {
                firstTableValue = firstTableIndex[firstIndexPointer++]
                firstTableBlock = memtableService.loadBlockByKey(firstTableIndex, firstTable.id, firstTableValue.key)
                firstBlockPointer = IntWrapper(0)
            }
            if (secondBlockPointer.get() == secondTableBlock.size) {
                secondTableValue = secondTableIndex[secondIndexPointer++]
                secondTableBlock = memtableService.loadBlockByKey(secondTableIndex, secondTable.id, secondTableValue.key)
                secondBlockPointer = IntWrapper(0)
            }
        }

        while (firstIndexPointer < firstTableIndex.size) {
            merger.insertFirstBlock()

            firstTableValue = firstTableIndex[firstIndexPointer++]
            firstTableBlock = memtableService.loadBlockByKey(firstTableIndex, firstTable.id, firstTableValue.key)
            firstBlockPointer = IntWrapper(0)
        }

        while (secondIndexPointer < secondTableIndex.size) {
            merger.insertSecondBlock()

            secondTableValue = secondTableIndex[secondIndexPointer++]
            secondTableBlock = memtableService.loadBlockByKey(secondTableIndex, secondTable.id, secondTableValue.key)
            secondBlockPointer = IntWrapper(0)
        }
    }

    private class IntWrapper(private var value: Int = 0) {
        fun get(): Int {
            return value
        }

        fun set(newValue: Int): IntWrapper {
            value = newValue
            return this
        }

        fun inc(): IntWrapper {
            value++
            return this
        }
    }

    private class BlockMerger(
        val firstTableBlock: List<AVLTree.Entry>,
        val secondTableBlock: List<AVLTree.Entry>,
        var firstBlockPointer: IntWrapper,
        var secondBlockPointer: IntWrapper,
        var currentBlockSize: IntWrapper,
        var newBlock: AVLTree,
        var cutOffBlockSize: Long,
        var memtableService: MemtableService,
        var tableWriter: BufferedOutputStream,
        var indexWriter: BufferedOutputStream,
    ) {
        fun merge() {
            while (firstBlockPointer.get() < firstTableBlock.size && secondBlockPointer.get() < secondTableBlock.size) {
                val firstBlockCurrentValue = firstTableBlock[firstBlockPointer.get()]
                val secondBlockCurrentValue = secondTableBlock[secondBlockPointer.get()]

                if (firstTableBlock[firstBlockPointer.get()].key < secondTableBlock[secondBlockPointer.get()].key) {
                    newBlock.upsert(firstBlockCurrentValue.key, firstBlockCurrentValue.value)
                    currentBlockSize.set(firstBlockCurrentValue.key.length + firstBlockCurrentValue.value.length)
                    firstBlockPointer.inc()
                } else {
                    newBlock.upsert(secondBlockCurrentValue.key, secondBlockCurrentValue.value)
                    currentBlockSize.set(secondBlockCurrentValue.key.length + secondBlockCurrentValue.value.length)
                    secondBlockPointer.inc()
                }

                if (currentBlockSize.get() >= cutOffBlockSize) {
                    memtableService.appendBlockToSSTable(newBlock, tableWriter, indexWriter)

                    currentBlockSize = IntWrapper(0)
                    newBlock = DefaultAVLTree()
                }
            }
        }

        private fun insertBlock(tableBlock: List<AVLTree.Entry>, blockPointer: IntWrapper) {
            while (blockPointer.get() < firstTableBlock.size) {
                val blockCurrentValue = tableBlock[blockPointer.get()]
                newBlock.upsert(blockCurrentValue.key, blockCurrentValue.value)
                currentBlockSize.set(blockCurrentValue.key.length + blockCurrentValue.value.length)
                firstBlockPointer.inc()

                if (currentBlockSize.get() >= cutOffBlockSize) {
                    memtableService.appendBlockToSSTable(newBlock, tableWriter, indexWriter)

                    currentBlockSize = IntWrapper(0)
                    newBlock = DefaultAVLTree()
                }
            }
        }

        fun insertFirstBlock() {
            insertBlock(firstTableBlock, firstBlockPointer)
        }

        fun insertSecondBlock() {
            insertBlock(secondTableBlock, secondBlockPointer)
        }
    }
}
