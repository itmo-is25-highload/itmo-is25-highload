package ru.itmo.storage.storage.lsm.replication.master

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.storage.storage.lsm.replication.utils.HttpUtils

@RestController
class MasterController(
    private val replicationService: ReplicationService
) {
    private val log = KotlinLogging.logger { }

    @PostMapping("accept-replica")
    fun acceptReplicaConnection(request: HttpServletRequest) {
        val replicaAddress = HttpUtils.getRequestIP()
        log.info { "Adding new sync replica $replicaAddress" }
        replicationService.addReplica(replicaAddress)
    }
}
