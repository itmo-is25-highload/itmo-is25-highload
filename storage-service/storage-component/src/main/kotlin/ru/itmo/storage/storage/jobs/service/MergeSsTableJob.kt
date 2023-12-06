@file:OptIn(ExperimentalPathApi::class)

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
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
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
    @OptIn(ExperimentalPathApi::class)
    override fun executeInternal(context: JobExecutionContext) {
//        return
        log.info { "Job execution started" }
        val tables = ssTableManager.getCurrentSSTables()
        if (tables.size < 2) {
            log.info { "Not enough SSTables to merge" }
            return
        }
        val firstTable = tables.pollFirst()
        val secondTable = tables.pollFirst()

        val newTable = memtableService.createEmptySSTable()
        val newTablePath = Path.of("${flushProperties.tableParentDir}/$newTable/table")
        val newTableIndexPath = Path.of("${flushProperties.tableParentDir}/$newTable/index")

        newTablePath.outputStream(StandardOpenOption.APPEND).buffered().use { tWriter ->
            newTableIndexPath.outputStream(StandardOpenOption.APPEND).buffered().use { iWriter ->
                val merger = SSTableMerger(firstTable, secondTable, tWriter, iWriter, newTable, newTablePath, memtableService, flushProperties)
                merger.mergeSSTables()
            }
        }

        val index = memtableService.loadIndex(newTable.toString())
        val ssTable = SSTable(newTable.toString(), index, BloomFilter(bloomFilterProperties.maxSize, index.size))
        tables.addFirst(ssTable)

        try {
            Path.of("${flushProperties.tableParentDir}/${firstTable.id}").deleteRecursively()
            Path.of("${flushProperties.tableParentDir}/${secondTable.id}").deleteRecursively()
        } catch (ex: IOException) {
            log.info { "Failure to remove older tables" }
            throw ex
        }
    }

    class SSTableMerger(
        val firstTable: SSTable,
        val secondTable: SSTable,
        val tableWriter: BufferedOutputStream,
        val indexWriter: BufferedOutputStream,
        val tableId: String,
        val table: Path,
        val memtableService: MemtableService,
        flushProperties: LsmRepositoryFlushProperties,
    ) {

        private val firstTableIndex = firstTable.index
        private val secondTableIndex = secondTable.index
        private var firstIndexPointer: Int = 0
        private var secondIndexPointer: Int = 0

        private var newBlock: DefaultAVLTree = DefaultAVLTree()
        private var currentNewBlockSize: Int = 0
        private var totalBytesWritten: Long = 0

        private var cutOffBlockSize: Long = flushProperties.blockSizeInBytes

        private lateinit var firstTableBlock: List<AVLTree.Entry>
        private lateinit var secondTableBlock: List<AVLTree.Entry>
        private var firstTableBlockPointer: Int = 0
        private var secondTableBlockPointer: Int = 0

        private lateinit var firstTableCurrentEntry: AVLTree.Entry
        private lateinit var secondTableCurrentEntry: AVLTree.Entry

        private var writtenKeys: MutableSet<String> = mutableSetOf()

        fun mergeSSTables() {
            firstTableCurrentEntry = firstTableIndex[firstIndexPointer++]
            secondTableCurrentEntry = secondTableIndex[secondIndexPointer++]

            firstTableBlock = memtableService.loadBlockByKey(firstTableIndex, firstTable.id, firstTableCurrentEntry.key)
            secondTableBlock = memtableService.loadBlockByKey(secondTableIndex, secondTable.id, secondTableCurrentEntry.key)

            while ((firstIndexPointer < firstTableIndex.size && secondIndexPointer < secondTableIndex.size) || (firstTableBlockPointer < firstTableBlock.size && secondTableBlockPointer < secondTableBlock.size)) {
                mergeBlocks()
                loadBlocksBetweenMerge()
            }

            mergeRemainingBlocksOfFirstTable()
            mergeRemainingBlocksOfSecondTable()

            if (currentNewBlockSize != 0) {
                flushBlock()
            }
        }

        private fun mergeBlocks() {
            while (firstTableBlockPointer < firstTableBlock.size && secondTableBlockPointer < secondTableBlock.size) {
                val firstBlockCurrentValue = firstTableBlock[firstTableBlockPointer]
                val secondBlockCurrentValue = secondTableBlock[secondTableBlockPointer]

                val firstBlockCurrentKey = firstBlockCurrentValue.key
                val secondBlockCurrentKey = secondBlockCurrentValue.key

                if (firstBlockCurrentKey < secondBlockCurrentKey) {
                    if (!writtenKeys.contains(firstBlockCurrentKey)) {
                        newBlock.upsert(firstBlockCurrentValue.key, firstBlockCurrentValue.value)
                        writtenKeys.add(firstBlockCurrentKey)
                        currentNewBlockSize += firstBlockCurrentValue.key.length + firstBlockCurrentValue.value.length
                    }
                    firstTableBlockPointer++
                } else {
                    if (!writtenKeys.contains(secondBlockCurrentKey)) {
                        newBlock.upsert(secondBlockCurrentValue.key, secondBlockCurrentValue.value)
                        writtenKeys.add(secondBlockCurrentKey)
                        currentNewBlockSize += secondBlockCurrentValue.key.length + secondBlockCurrentValue.value.length
                    }
                    secondTableBlockPointer++
                }

                if (currentNewBlockSize >= cutOffBlockSize) {
                    flushBlock()
                }
            }
        }

        private fun loadBlocksBetweenMerge() {
            if (firstTableBlockPointer == firstTableBlock.size && firstIndexPointer < firstTableIndex.size) {
                firstTableCurrentEntry = firstTableIndex[firstIndexPointer++]
                firstTableBlock = memtableService.loadBlockByKey(firstTableIndex, firstTable.id, firstTableCurrentEntry.key)
                firstTableBlockPointer = 0
            }
            if (secondTableBlockPointer == secondTableBlock.size && secondIndexPointer < secondTableIndex.size) {
                secondTableCurrentEntry = secondTableIndex[secondIndexPointer++]
                secondTableBlock = memtableService.loadBlockByKey(secondTableIndex, secondTable.id, secondTableCurrentEntry.key)
                secondTableBlockPointer = 0
            }
        }

        private fun mergeRemainingBlocksOfFirstTable() {
            do {
                while (firstTableBlockPointer < firstTableBlock.size) {
                    val blockCurrentValue = firstTableBlock[firstTableBlockPointer]
                    if (!writtenKeys.contains(blockCurrentValue.key)) {
                        newBlock.upsert(blockCurrentValue.key, blockCurrentValue.value)
                        writtenKeys.add(blockCurrentValue.key)
                        currentNewBlockSize += blockCurrentValue.key.length + blockCurrentValue.value.length
                    }
                    firstTableBlockPointer++

                    if (currentNewBlockSize >= cutOffBlockSize) {
                        flushBlock()
                    }

                    if (firstIndexPointer < firstTableIndex.size) {
                        firstTableCurrentEntry = firstTableIndex[firstIndexPointer++]
                        firstTableBlock = memtableService.loadBlockByKey(firstTableIndex, firstTable.id, firstTableCurrentEntry.key)
                        firstTableBlockPointer = 0
                    }
                }
            } while (firstIndexPointer < firstTableIndex.size)
        }

        private fun mergeRemainingBlocksOfSecondTable() {
            do {
                while (secondTableBlockPointer < secondTableBlock.size) {
                    val blockCurrentValue = secondTableBlock[secondTableBlockPointer]
                    if (!writtenKeys.contains(blockCurrentValue.key)) {
                        newBlock.upsert(blockCurrentValue.key, blockCurrentValue.value)
                        writtenKeys.add(blockCurrentValue.key)
                        currentNewBlockSize += blockCurrentValue.key.length + blockCurrentValue.value.length
                    }
                    secondTableBlockPointer++

                    if (currentNewBlockSize >= cutOffBlockSize) {
                        flushBlock()
                    }

                    if (secondIndexPointer < secondTableIndex.size) {
                        secondTableCurrentEntry = secondTableIndex[secondIndexPointer++]
                        secondTableBlock = memtableService.loadBlockByKey(
                            secondTableIndex,
                            secondTable.id,
                            secondTableCurrentEntry.key,
                        )
                        secondTableBlockPointer = 0
                    }
                }
            } while (secondIndexPointer < secondTableIndex.size)
        }

        private fun flushBlock() {
            memtableService.appendBlockToSSTable(newBlock, tableWriter, indexWriter, table, tableId)
            totalBytesWritten += currentNewBlockSize
            currentNewBlockSize = 0
            newBlock = DefaultAVLTree()
            tableWriter.flush()
        }
    }
}
