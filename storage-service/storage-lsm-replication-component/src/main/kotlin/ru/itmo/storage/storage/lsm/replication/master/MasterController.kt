package ru.itmo.storage.storage.lsm.replication.master

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import ru.itmo.storage.storage.lsm.replication.common.ReplicaAddress

@RestController
class MasterController(
    private val replicationService: ReplicationService
) {
    private val log = KotlinLogging.logger { }

    @PostMapping("accept-replica")
    fun acceptReplicaConnection(@RequestBody replicaAddress: ReplicaAddress) {
        log.info { "Adding new sync replica ${replicaAddress.value}" }
        replicationService.addReplica(replicaAddress.value)
    }
}
