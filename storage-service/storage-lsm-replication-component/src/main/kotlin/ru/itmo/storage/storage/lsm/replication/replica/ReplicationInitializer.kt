package ru.itmo.storage.storage.lsm.replication.replica

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import ru.itmo.storage.storage.lsm.replication.exception.MasterUnavailableException
import java.io.File

@Component
class ReplicationInitializer(
    private val replicaRestTemplate: RestTemplate
) {
    private val log = KotlinLogging.logger { }

    @Value("\${storage.component.flush.tableParentDir:replica-data}")
    private lateinit var ssTablesPath: String

    @Value("\${logs.dir:replica-logs}")
    private lateinit var logsPath: String

    @PostConstruct
    private fun initialize() {
        log.info { "Initializing replica" }
        replicaRestTemplate.exchange("/accept-replica", HttpMethod.POST, null, String::class.java)
            .also {
                if (!it.statusCode.is2xxSuccessful) {
                    throw MasterUnavailableException()
                }
            }

    }

    fun addFiles(wal: MultipartFile, ssTables: List<MultipartFile>) {
        saveMultipartFile(wal, "$logsPath/wal.log")
        for (ssTable in ssTables) {
            saveMultipartFile(ssTable, "$ssTablesPath/${ssTable.name}")
        }
    }

    private fun saveMultipartFile(multipartFile: MultipartFile, path: String) {
        val file = File(path)
        multipartFile.transferTo(file)
    }
}
