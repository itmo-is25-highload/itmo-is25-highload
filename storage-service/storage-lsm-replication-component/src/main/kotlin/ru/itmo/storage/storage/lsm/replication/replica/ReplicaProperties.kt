package ru.itmo.storage.storage.lsm.replication.replica

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.lsm.replication.replica")
class ReplicaProperties {
    lateinit var replicaOf: String
}
