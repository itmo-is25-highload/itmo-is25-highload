package ru.itmo.storage.storage.lsm.replication.master

interface ReplicationService {
    fun sendToSyncReplicas(key: String, value: String)
    fun addReplica(replicaAddress: String)
}
