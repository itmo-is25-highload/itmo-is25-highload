package ru.itmo.storage.storage.lsm.sstable

import org.springframework.stereotype.Service
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.bloomfilter.BloomFilter
import ru.itmo.storage.storage.lsm.properties.BloomFilterProperties
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory

@Service
class LocalSSTableLoader(
    private val flushProperties: LsmRepositoryFlushProperties,
    private val bloomFilterProperties: BloomFilterProperties,
    private val memtableService: MemtableService,
) : SSTableLoader {

    override fun loadTablesSortedByCreationTimeDesc(): List<SSTable> {
        if (!Path.of(flushProperties.tableParentDir).isDirectory()) {
            return ArrayList()
        }
        return Files.walk(Paths.get(flushProperties.tableParentDir), 1)
            .filter { !Paths.get(flushProperties.tableParentDir).equals(it) && Files.isDirectory(it) }
            .sorted { path1, path2 -> compareByCreationTimeDesc(path1, path2) }
            .map { SSTable(it.fileName.toString(), memtableService.loadIndex(it.fileName.toString()), BloomFilter(bloomFilterProperties.maxSize, 0)) }
            .toList()
    }

    private fun compareByCreationTimeDesc(path1: Path, path2: Path): Int {
        return path1.compareTo(path2)
    }
}
