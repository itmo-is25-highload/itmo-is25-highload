package ru.itmo.storage.storage.lsm.sstable

import org.springframework.stereotype.Service
import ru.itmo.storage.storage.lsm.MemtableService
import ru.itmo.storage.storage.lsm.properties.LsmRepositoryFlushProperties
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory

@Service
class LocalSSTableLoader(
    private val properties: LsmRepositoryFlushProperties,
    private val memtableService: MemtableService
) : SSTableLoader {

    override fun loadTables(): List<SSTable> {
        if (!Path.of(properties.tableParentDir).isDirectory()) {
            return ArrayList()
        }
        return Files.walk(Paths.get(properties.tableParentDir), 1)
            .filter { !Paths.get(properties.tableParentDir).equals(it) && Files.isDirectory(it) }
            .map { SSTable(it.fileName.toString(), memtableService.loadIndex(it.fileName.toString())) }
            .toList()
    }
}
