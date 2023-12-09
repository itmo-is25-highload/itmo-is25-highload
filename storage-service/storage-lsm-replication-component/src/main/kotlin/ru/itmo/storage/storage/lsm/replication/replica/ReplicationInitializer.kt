package ru.itmo.storage.storage.lsm.replication.replica

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import ru.itmo.storage.storage.lsm.replication.exception.MasterUnavailableException

@Component
class ReplicationInitializer(
    private val replicaRestTemplate: RestTemplate
) {
    private val log = KotlinLogging.logger { }

    @Value("\${storage.component.flush.tableParentDir:replica-data}")
    private lateinit var ssTablesPath: String

    @Value("\${logs.dir:replica-logs}")
    private lateinit var walPath: String

    @PostConstruct
    private fun initialize() {
        log.info { "Initializing replica" }
        replicaRestTemplate.exchange("accept-replica", HttpMethod.POST, null, String::class.java)
            .also {
                if (!it.statusCode.is2xxSuccessful) {
                    throw MasterUnavailableException()
                }
            }

    }
}
