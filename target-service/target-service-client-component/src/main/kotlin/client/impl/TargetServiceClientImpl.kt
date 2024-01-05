package ru.itmo.target.client.impl

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import ru.itmo.target.client.TargetServiceClient
import ru.itmo.target.properties.ClientProperties

@Component
class TargetServiceClientImpl(private val client: WebClient, private val properties: ClientProperties) :
    TargetServiceClient {
    override fun deviousGriddy(): String {
        return client.get()
            .uri { uriBuilder -> uriBuilder.path(properties.serviceUrl).path("/griddy/devious").build() }
            .retrieve()
            .bodyToMono(String::class.java)
            .block()!!
    }

    override fun mcGriddy(): String {
        return client.get()
            .uri { uriBuilder -> uriBuilder.path(properties.serviceUrl).path("/griddy/mc").build() }
            .retrieve()
            .bodyToMono(String::class.java)
            .block()!!
    }
}
