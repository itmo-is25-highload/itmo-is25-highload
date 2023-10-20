package ru.itmo.storage.storage.lsm

import org.springframework.stereotype.Service
import ru.itmo.storage.storage.compression.CompressionService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.avl.DefaultAVLTree
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
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

    override fun flush(memtable: AVLTree) {
        val tableDirPath = getDirectoryPath().createDirectory()
        val table = Files.createFile(Path.of("$tableDirPath/table"))
        val index = Files.createFile(Path.of("$tableDirPath/index"))

        table.outputStream().use { tWriter ->
            index.outputStream().use { iWriter ->
                writeToDisk(tWriter, iWriter, memtable)
            }
        }
    }

    override fun load(tableId: String): AVLTree {
        val index = Path.of("${properites.tableParentDir}/$tableId/index")
        index.inputStream().buffered().use { iReader ->
            return readIndex(iReader)
        }
    }

    override fun loadBlock(memtable: AVLTree, tableId: String, blockKey: String): List<Pair<String, String>> {
        val table = Path.of("${properites.tableParentDir}/$tableId/table")
        val index = Path.of("${properites.tableParentDir}/$tableId/index")
        val entities = memtable.orderedEntries()

        val blockIndex = memtable.orderedEntries().indexOfFirst { entry -> entry.key == blockKey }
        val entry = entities[blockIndex]
        val size: Long

        if (blockIndex == entities.size - 1) {
            size = index.fileSize() - entry.value.toLong()
        } else {
            size = entities[blockIndex + 1].value.toLong() - entry.value.toLong()
        }

        table.inputStream().buffered().use { iReader ->
            return readTable(iReader, size)
        }
    }

    private fun readTable(tableReader: BufferedInputStream, size: Long): List<Pair<String, String>> {
        val buffer = ByteArray(size.toInt())
        tableReader.read(buffer)

        val decompressed = compressionService.decompress(buffer)
        val tokenizerMachine = TokenizerMachine(decompressed)
        return tokenizerMachine.tokenize()
    }

    private fun readIndex(indexReader: BufferedInputStream): AVLTree {
        val tree: AVLTree = DefaultAVLTree()
        val baos = ByteArrayOutputStream()

        val buffer = ByteArray(4096)

        var currentReadSize = indexReader.read(buffer)
        var totalBytesRead = currentReadSize

        while (currentReadSize != -1) {
            baos.write(buffer, totalBytesRead, currentReadSize)
            totalBytesRead += currentReadSize
            currentReadSize = indexReader.read(buffer)
        }

        val tokenizer = TokenizerMachine(baos.toByteArray())
        val tokenizedIndex = tokenizer.tokenize()

        for (token in tokenizedIndex) {
            tree.upsert(token.first, token.second)
        }

        return tree
    }

    private fun getDirectoryPath(): Path {
        var dirId = UUID.randomUUID()
        val tablePathPrefix = properites.tableParentDir
        while (Path.of("$tablePathPrefix/$dirId").exists())
            dirId = UUID.randomUUID()

        return Path.of("$tablePathPrefix/$dirId")
    }

    private fun writeToDisk(tableWriter: OutputStream, indexWriter: OutputStream, memtable: AVLTree) {
        val entities = memtable.orderedEntries()
        var lastBlockKey: String? = null

        var indexOffset: Long = 0
        var blockStartOffset: Long = 0
        var currentBlockSize: Long = 0

        val baos = ByteArrayOutputStream()
        var endOfBlock = true

        if (entities.isEmpty()) {
            return
        }

        for (i in entities.indices) {
            val key = entities[i].key
            val value = entities[i].value

            if (endOfBlock) {
                lastBlockKey = key
                blockStartOffset = indexOffset
                endOfBlock = false
            }

            val currentEntry = "${key.escapeEntry()}$properites.keyValueDelimiter${value.escapeEntry()}$properites.blockEntryDelimiter".toByteArray()
            baos.write(currentEntry, currentBlockSize.toInt(), currentEntry.size)

            currentBlockSize += currentEntry.size
            indexOffset += currentBlockSize

            if (currentBlockSize >= properites.blockSizeInBytes) {
                val compressedBlock = compressionService.compress(baos.toByteArray())
                val indexEntry = "${key.escapeEntry()}$properites.keyValueDelimiter$indexOffset$properites.blockEntryDelimiter"

                endOfBlock = true
                currentBlockSize = 0

                tableWriter.write(compressedBlock)
                indexWriter.write(indexEntry.toByteArray())

                baos.reset()
            }
        }

        if (endOfBlock) {
            return
        }

        tableWriter.write(compressionService.compress(baos.toByteArray()))
        indexWriter.write("${lastBlockKey?.escapeEntry()}$properites.keyValueDelimiter$blockStartOffset$properites.blockEntryDelimiter".toByteArray())
    }

    private fun String.escape(delimiter: String): String {
        return this.replace(delimiter, "\\$delimiter")
    }

    private fun String.escapeEntry(): String {
        return this.escape("\\").escape(properites.keyValueDelimiter).escape(properites.entryDelimiter)
    }

    private class TokenizerMachine(byteArray: ByteArray) {
        private var isDone: Boolean = false
        private var isKey: Boolean = true
        private var isPreviousCharEscape: Boolean = false
        private var keyValueList = mutableListOf<Pair<String, String>>()
        private val iterator = byteArray.toString().iterator()
        private val sb = StringBuilder()

        public fun tokenize(): List<Pair<String, String>> {
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
                    if (!(processKeyValueDelimiter(x) && processEntryDelimiter(x))) {
                        sb.append(x)
                    }
                }
            }
            isDone = true
            return keyValueList
        }

        private fun processKeyValueDelimiter(character: Char): Boolean {
            if (character == ':' && isKey && isPreviousCharEscape) {
                isKey = false
                keyValueList.add(Pair(sb.toString(), ""))
                sb.clear()
                return true
            }
            return false
        }

        private fun processEntryDelimiter(character: Char): Boolean {
            if (character == ';' && !isPreviousCharEscape && !isKey) {
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
