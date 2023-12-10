package ru.itmo.storage.storage.lsm.replication.master

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import ru.itmo.storage.storage.lsm.core.AVLTree
import ru.itmo.storage.storage.lsm.core.sstable.SSTableManager
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

@Service
class ReplicationServiceImpl(
    private val masterReplicationRestTemplate: RestTemplate,
    private val ssTableManager: SSTableManager
) : ReplicationService {

    @Value("\${storage.component.flush.tableParentDir}")
    private lateinit var ssTablesPath: String

    @Value("\${logs.dir:logs}")
    private lateinit var logsPath: String

    private val log = KotlinLogging.logger { }
    private val replicas: MutableSet<String> = HashSet()

    override fun sendToSyncReplicas(key: String, value: String) {
        replicas.forEach {
            log.info { "Syncing with replica $it" }
            val entity = getTreeEntryHttpEntity(key, value)
            val replicaResponse = masterReplicationRestTemplate.exchange(
                getReplicaSyncUrl(it),
                HttpMethod.POST,
                entity,
                String::class.java
            )

            if (!replicaResponse.statusCode.is2xxSuccessful) {
                log.error { "Failed to sync with replica $it, status is ${replicaResponse.statusCode}" }
            }
        }
    }


    override fun addReplica(replicaAddress: String): HttpEntity<MultiValueMap<String, Any>> {
        replicas.add(replicaAddress)
        return createDataTransitionRequest()
    }

    private fun createDataTransitionRequest(): HttpEntity<MultiValueMap<String, Any>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        val walFileEntity = getFileEntity("$logsPath/wal.log", "wal", logsPath)
        if (walFileEntity != null) {
            body.add("wal", walFileEntity)
        }
        for (ssTable in ssTableManager.getCurrentSSTables()) {
            body.add("sstables", getFileEntity("$ssTablesPath/${ssTable.id}/index", "sstables", ssTablesPath))
            body.add("sstables", getFileEntity("$ssTablesPath/${ssTable.id}/table", "sstables", ssTablesPath))
        }

        return HttpEntity(body, headers)
    }

    private fun getFileEntity(path: String, name: String, parentDir: String): HttpEntity<ByteArray>? {
        val file = File(path)
        if (!file.exists()) {
            return null
        }

        val newName = Path(path).relativeTo(Path(parentDir))
        val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        val contentDisposition: ContentDisposition = ContentDisposition
            .builder("form-data")
            .name(name)
            .filename(newName.pathString)
            .build()

        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        return HttpEntity(Files.readAllBytes(file.toPath()), fileMap)
    }

    private fun getReplicaSyncUrl(replicaAddress: String): String {
        return "$replicaAddress/sync"
    }

    private fun getTreeEntryHttpEntity(
        key: String,
        value: String
    ): HttpEntity<AVLTree.Entry> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(AVLTree.Entry(key, value), headers)
    }
}
