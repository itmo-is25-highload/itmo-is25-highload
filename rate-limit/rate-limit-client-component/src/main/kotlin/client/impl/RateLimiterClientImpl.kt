package ru.itmo.ratelimit.client.impl

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import ru.itmo.ratelimit.client.RateLimiterClient
import ru.itmo.ratelimit.client.configuration.RateLimiterClientProperties

@Component
class RateLimiterClientImpl(private val webClient: WebClient, private val properties: RateLimiterClientProperties) : RateLimiterClient {
    override fun limit(key: String, value: String?): Boolean {
        val requestSpec = webClient.get()
            .uri { uriBuilder -> uriBuilder.path(properties.Url).queryParam("value", value).build(key) }
            .retrieve()
            .bodyToMono<Boolean>()
            .block()

        return requestSpec!!
    }
}
