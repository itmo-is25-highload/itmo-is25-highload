package ru.itmo.storage.storage.lsm

import org.springframework.stereotype.Service
import ru.itmo.storage.storage.compression.CompressionService
import ru.itmo.storage.storage.lsm.avl.AVLTree
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.outputStream

@Service
class DefaultMemtableFlushService(
    private val properites: LsmRepositoryFlushProperties,
    private val compressionService: CompressionService,
) : MemtableFlushService {

    private val parentDirectoryPath: Path = Path.of(properites.tableParentDir)

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

    private fun getDirectoryPath(): Path {
        var dirId = UUID.randomUUID()
        val tablePathPrefix = "${parentDirectoryPath.toAbsolutePath()}"
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

        val keyValueDelimiter = properites.keyValueDelimiter
        val entryDelimiter = properites.entryDelimiter
        var newBlock = true

        if (entities.isEmpty()) {
            return
        }

        for (i in entities.indices) {
            val key = entities[i].key
            val value = entities[i].value

            if (newBlock) {
                lastBlockKey = key
                blockStartOffset = indexOffset
                newBlock = false
            }

            val currentEntry = "${key.escape(keyValueDelimiter).escape(entryDelimiter)}$properites.keyValueDelimiter${value.escape(keyValueDelimiter).escape(entryDelimiter)}$properites.blockEntryDelimiter".toByteArray()
            baos.write(currentEntry, currentBlockSize.toInt(), currentEntry.size)

            currentBlockSize += currentEntry.size
            indexOffset += currentBlockSize

            if (currentBlockSize >= properites.blockSizeInBytes) {
                val compressedBlock = compressionService.compress(baos.toByteArray())
                val indexEntry = "${key.escape(keyValueDelimiter).escape(entryDelimiter)}$properites.keyValueDelimiter$indexOffset$properites.blockEntryDelimiter"

                newBlock = true
                currentBlockSize = 0

                tableWriter.write(compressedBlock)
                indexWriter.write(indexEntry.toByteArray())

                baos.reset()
            }
        }

        if (newBlock) {
            return
        }

        tableWriter.write(compressionService.compress(baos.toByteArray()))
        indexWriter.write("${lastBlockKey?.escape(keyValueDelimiter)?.escape(entryDelimiter)}$properites.keyValueDelimiter$blockStartOffset$properites.blockEntryDelimiter".toByteArray())
    }

    fun String.escape(delimiter: String): String {
        return this.replace(properites.entryDelimiter, "\\$delimiter")
    }
}
