package ru.itmo.storage.storage.lsm.replication.replica

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader
import org.springframework.http.codec.multipart.MultipartHttpMessageReader
import org.springframework.http.codec.multipart.Part
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ReplicaConfiguration(
    private val replicaProperties: ReplicaProperties
) {
    @Bean
    fun partReader(): HttpMessageReader<Part> {
        return DefaultPartHttpMessageReader()
    }

    @Bean
    fun replicaWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(replicaProperties.replicaOf)
            .codecs { clientCodecConfigurer ->
                clientCodecConfigurer.customCodecs().register(MultipartHttpMessageReader(partReader()))
            }
            .build()
    }
}