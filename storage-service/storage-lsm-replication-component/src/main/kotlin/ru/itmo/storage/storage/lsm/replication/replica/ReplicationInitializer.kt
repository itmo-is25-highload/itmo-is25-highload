package ru.itmo.storage.storage.lsm.replication.replica

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.Part
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import ru.itmo.storage.storage.lsm.replication.common.ReplicaAddress
import ru.itmo.storage.storage.lsm.replication.exception.MasterUnavailableException
import java.io.File
import java.net.InetAddress
import java.nio.file.Path
import kotlin.io.path.createDirectories

@Component
class ReplicationInitializer(
    private val replicaWebClient: WebClient,
) {
    private val log = KotlinLogging.logger { }

    @Value("\${storage.component.flush.tableParentDir:replica-data}")
    private lateinit var ssTablesPath: String

    @Value("\${logs.dir:replica-logs}")
    private lateinit var logsPath: String

    @Value("\${server.port}")
    private val port: Int = 8080

    fun initialize() {
        log.info { "Initializing replica" }
        val response = replicaWebClient.post()
            .uri("/accept-replica")
            .body(BodyInserters.fromValue(getAddress()))
            .accept(MediaType.MULTIPART_FORM_DATA)
            .retrieve()
            .toEntity(object: ParameterizedTypeReference<MultiValueMap<String, Part>>() {})
            .block()

        val responseBody = response?.body ?: throw MasterUnavailableException()

        val wal = responseBody["wal"]?.get(0) ?: throw MasterUnavailableException()
        saveFilePart(wal, logsPath)
        for (sstable in responseBody["sstables"]!!) {
            saveFilePart(sstable, ssTablesPath)
        }
    }

    private fun saveFilePart(part: Part, rootDir: String) {
        val fileByteArray = getByteArray(part) ?: throw MasterUnavailableException()
        val filename = part.headers()["Content-Disposition"]?.let { ContentDisposition.parse(it[0]).filename }
        val path = Path.of("$rootDir/$filename")
        path.parent?.createDirectories()
        File(path.toUri()).writeBytes(fileByteArray)
    }

    private fun getByteArray(part: Part): ByteArray? {
        return DataBufferUtils.join(part.content())
            .map {
                val bytes = ByteArray(it.readableByteCount())
                it.read(bytes)
                DataBufferUtils.release(it)
                bytes
            }.block()
    }

    private fun getAddress(): ReplicaAddress {
        //TODO http:// - ?
        return ReplicaAddress("http://${InetAddress.getLoopbackAddress().hostAddress}:$port")
    }
}
