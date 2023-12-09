package ru.itmo.storage.storage.lsm.replication.replica

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class ReplicaConfiguration(
    private val replicaProperties: ReplicaProperties
) {

    @Bean
    fun replicaRestTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(replicaProperties.replicaOf)
            .build()
    }
}