package ru.itmo.ratelimit.client.impl

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import ru.itmo.contracts.ratelimit.RateLimitIncrementRequest
import ru.itmo.contracts.ratelimit.RateLimiterResponse
import ru.itmo.ratelimit.client.RateLimiterClient

@Component
class RateLimiterClientImpl(private val webClient: WebClient) : RateLimiterClient {
    override fun checkLimit(key: String, value: String?): RateLimiterResponse {
        val requestSpec = webClient.get()
            .uri { uriBuilder -> uriBuilder.path("/checkLimit/$key").queryParam("value", value).build(key) }
            .retrieve()
            .bodyToMono<RateLimiterResponse>()
            .block()

        return requestSpec!!
    }

    override fun incrementLimit(key: String, value: String?) {
        val request = RateLimitIncrementRequest(key, value)

        val requestSpec = webClient.post()
            .uri { uriBuilder -> uriBuilder.path("/incrementRequests").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<Void>()
            .block()
    }
}
