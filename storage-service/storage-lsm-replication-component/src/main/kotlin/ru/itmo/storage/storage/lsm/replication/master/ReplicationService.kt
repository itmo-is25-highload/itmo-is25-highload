package ru.itmo.storage.storage.lsm.replication.master

import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap

interface ReplicationService {
    fun sendToSyncReplicas(key: String, value: String)
    fun addReplica(replicaAddress: String): HttpEntity<MultiValueMap<String, Any>>
}
