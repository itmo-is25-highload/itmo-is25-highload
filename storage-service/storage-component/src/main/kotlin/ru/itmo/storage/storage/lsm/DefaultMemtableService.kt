package ru.itmo.storage.storage.lsm

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.itmo.storage.storage.compression.CompressionService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Service
class DefaultMemtableService(
    private val properites: LsmRepositoryFlushProperties,
    private val compressionService: CompressionService,
) : MemtableService {

    private val log = KotlinLogging.logger { }

    override fun flushMemtableToDisk(memtable: AVLTree): UUID {
        val directoryId = getDirectoryId()
        val tableDirPath = Path.of("${properites.tableParentDir}/$directoryId").createDirectory()

        log.info { "Save SSTable to $tableDirPath" }
        val table = Files.createFile(Path.of("$tableDirPath/table"))
        val index = Files.createFile(Path.of("$tableDirPath/index"))

        table.outputStream().use { tWriter ->
            index.outputStream().use { iWriter ->
                writeToDisk(tWriter, iWriter, memtable)
            }
        }

        return directoryId
    }

    override fun loadIndex(tableId: String): List<AVLTree.Entry> {
        val index = Path.of("${properites.tableParentDir}/$tableId/index")
        index.inputStream().buffered().use { iReader ->
            return readIndex(iReader)
        }
    }

    override fun loadBlockByKey(
        memtable: List<AVLTree.Entry>,
        tableId: String,
        blockKey: String,
    ): List<Pair<String, String>> {
        val table = Path.of("${properites.tableParentDir}/$tableId/table")

        val blockIndex = memtable.indexOfFirst { entry -> entry.key == blockKey }
        val entry = memtable[blockIndex]
        val size: Long

        if (blockIndex == memtable.size - 1) {
            size = table.fileSize() - entry.value.toLong()
        } else {
            size = memtable[blockIndex + 1].value.toLong() - entry.value.toLong()
        }

        table.inputStream().buffered().use { iReader ->
            return readTable(iReader, size)
        }
    }

    private fun readTable(tableReader: BufferedInputStream, size: Long): List<Pair<String, String>> {
        val buffer = ByteArray(size.toInt())
        tableReader.read(buffer)

        val decompressed = compressionService.decompress(buffer)
        val tokenizerMachine = TokenizerMachine(decompressed, properites.keyValueDelimiter, properites.entryDelimiter)
        return tokenizerMachine.tokenize()
    }

    private fun readIndex(indexReader: BufferedInputStream): List<AVLTree.Entry> {
        val baos = ByteArrayOutputStream()
        val result = mutableListOf<AVLTree.Entry>()

        val buffer = ByteArray(properites.readBufferSize.toInt())

        var currentReadSize = indexReader.read(buffer)
        var totalBytesRead = currentReadSize

        while (currentReadSize != -1) {
            baos.write(buffer, 0, currentReadSize)
            currentReadSize = indexReader.read(buffer)
            totalBytesRead += currentReadSize
        }

        val tokenizer = TokenizerMachine(baos.toByteArray(), properites.keyValueDelimiter, properites.entryDelimiter)
        val tokenizedIndex = tokenizer.tokenize()

        for (token in tokenizedIndex) {
            result += AVLTree.Entry(token.first, token.second)
        }

        return result.sortedWith(compareBy(AVLTree.Entry::key, AVLTree.Entry::value))
    }

    private fun getDirectoryId(): UUID {
        var dirId = UUID.randomUUID()
        val tablePathPrefix = properites.tableParentDir
        while (Path.of("$tablePathPrefix/$dirId").exists())
            dirId = UUID.randomUUID()

        return dirId
    }

    private fun writeToDisk(tableWriter: OutputStream, indexWriter: OutputStream, memtable: AVLTree) {
        val entities = memtable.orderedEntries()

        log.info { "Create SSTable with entries $entities" }

        if (entities.isEmpty()) {
            return
        }

        val baos = ByteArrayOutputStream()

        var indexOffset: Long = 0
        var blockStartOffset: Long = 0
        var currentBlockSize: Long = 0

        var firstEntry = true
        var currentIndexKey: String? = null

        val kvDelimiter = properites.keyValueDelimiter
        val entryDelimiter = properites.entryDelimiter
        for (i in entities.indices) {
            val key = entities[i].key
            val value = entities[i].value

            if (firstEntry) {
                currentBlockSize = 0
                currentIndexKey = key
                blockStartOffset = indexOffset
                firstEntry = false
            }

            val currentEntry = "${key.escapeEntry()}$kvDelimiter${value.escapeEntry()}$entryDelimiter".toByteArray()
            baos.write(currentEntry, 0, currentEntry.size)

            currentBlockSize += currentEntry.size
            indexOffset += currentBlockSize

            if (currentBlockSize >= properites.blockSizeInBytes) {
                val compressedBlock = compressionService.compress(baos.toByteArray())
                val indexEntry =
                    "${currentIndexKey?.escapeEntry()}$kvDelimiter$indexOffset$entryDelimiter"

                firstEntry = true

                tableWriter.write(compressedBlock)
                indexWriter.write(indexEntry.toByteArray())

                baos.reset()
            }
        }

        // Means that the last entry was loaded with the last block and there was no hanging entries
        if (!firstEntry) {
            tableWriter.write(compressionService.compress(baos.toByteArray()))
            indexWriter.write("${currentIndexKey?.escapeEntry()}$kvDelimiter${blockStartOffset}$entryDelimiter".toByteArray())
        }

        val lastKey = entities.last().key
        indexWriter.write("${lastKey.escapeEntry()}$kvDelimiter${indexOffset}$entryDelimiter".toByteArray())
    }

    private fun String.escape(delimiter: String): String {
        return this.replace(delimiter, "\\$delimiter")
    }

    private fun String.escapeEntry(): String {
        return this.escape("\\").escape(properites.keyValueDelimiter).escape(properites.entryDelimiter)
    }

    private class TokenizerMachine(
        byteArray: ByteArray,
        private val keyValueDelimiter: String,
        private val entryDelimiter: String,
    ) {
        private var isDone: Boolean = false
        private var isKey: Boolean = true
        private var isPreviousCharEscape: Boolean = false
        private var keyValueList = mutableListOf<Pair<String, String>>()

        // ¯\_(ツ)_/¯
        private val iterator = byteArray.toString(StandardCharsets.UTF_8).iterator()
        private val sb = StringBuilder()

        fun tokenize(): List<Pair<String, String>> {
            if (isDone) {
                return keyValueList
            }

            for (x in iterator) {
                if (x == '\\') {
                    if (isPreviousCharEscape) {
                        isPreviousCharEscape = false
                        sb.append(x)
                    } else {
                        isPreviousCharEscape = true
                    }
                } else {
                    if (processKeyValueDelimiter(x)) {
                    } else if (processEntryDelimiter(x)) {
                    } else {
                        sb.append(x)
                    }

                    if (isPreviousCharEscape) {
                        isPreviousCharEscape = false
                    }
                }
            }
            isDone = true
            return keyValueList
        }

        // Сломатется на любом разделителе длины больше одного.
        private fun processKeyValueDelimiter(character: Char): Boolean {
            if (character.toString() == keyValueDelimiter && isKey && !isPreviousCharEscape) {
                isKey = false
                keyValueList.add(Pair(sb.toString(), ""))
                sb.clear()
                return true
            }
            return false
        }

        private fun processEntryDelimiter(character: Char): Boolean {
            if (character.toString() == entryDelimiter && !isPreviousCharEscape && !isKey) {
                isKey = true
                val key = keyValueList[keyValueList.size - 1].first
                keyValueList[keyValueList.size - 1] = Pair(key, sb.toString())
                sb.clear()
                return true
            }
            return false
        }
    }
}
