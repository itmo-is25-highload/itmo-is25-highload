package ru.itmo.storage.storage.lsm.replication.master

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.itmo.storage.storage.lsm.core.AVLTree

@Service
class ReplicationServiceImpl(
    private val masterReplicationRestTemplate: RestTemplate
) : ReplicationService {

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


    override fun addReplica(replicaAddress: String) {
        replicas.add(replicaAddress)
    }

    private fun getReplicaSyncUrl(it: String): String {
        return "$it/sync"
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
