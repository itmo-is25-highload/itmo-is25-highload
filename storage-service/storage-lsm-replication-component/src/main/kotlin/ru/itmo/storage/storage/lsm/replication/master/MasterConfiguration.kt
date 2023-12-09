package ru.itmo.storage.storage.lsm.replication.master

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class MasterConfiguration {

    @Bean
    fun masterReplicationRestTemplate(): RestTemplate {
        return RestTemplate()
    }
}