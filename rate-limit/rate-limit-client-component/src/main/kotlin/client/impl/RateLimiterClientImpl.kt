package client.impl

import client.RateLimiterClient
import org.springframework.web.reactive.function.client.WebClient

class RateLimiterClientImpl(private val webClient: WebClient) : RateLimiterClient {
    override fun limit(key: String, value: String?): Boolean {
        val requestSpec = webClient.get()
            .uri { uriBuilder -> uriBuilder.path("/keys/{key}").queryParam("value", value).build(key) }
        TODO("Not implemented")
    }

}