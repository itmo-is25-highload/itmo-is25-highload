package ru.itmo.storage.storage.lsm.sstable

import org.springframework.stereotype.Service
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import kotlin.io.path.isDirectory

@Service
class LocalSSTableLoader(
    private val properties: LsmRepositoryFlushProperties,
    private val memtableService: MemtableService
) : SSTableLoader {

    override fun loadTablesSortedByCreationTimeDesc(): List<SSTable> {
        if (!Path.of(properties.tableParentDir).isDirectory()) {
            return ArrayList()
        }
        return Files.walk(Paths.get(properties.tableParentDir), 1)
            .filter { !Paths.get(properties.tableParentDir).equals(it) && Files.isDirectory(it) }
            .sorted { path1, path2 -> compareByCreationTimeDesc(path1, path2) }
            .map { SSTable(it.fileName.toString(), memtableService.loadIndex(it.fileName.toString())) }
            .toList()
    }

    private fun compareByCreationTimeDesc(path1: Path, path2: Path): Int {
        val creationTime1 = getCreationTime(path1)
        val creationTime2 = getCreationTime(path2)
        return creationTime2.compareTo(creationTime1)
    }

    private fun getCreationTime(path: Path): FileTime =
        Files.readAttributes(path, BasicFileAttributes::class.java).creationTime()
}
