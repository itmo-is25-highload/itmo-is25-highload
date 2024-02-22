package ru.itmo.ratelimit.client.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebclientConfig {
    @Bean
    fun webClient(builder: WebClient.Builder, properties: RateLimiterClientProperties): WebClient {
        return builder.baseUrl(properties.url).build()
    }
}
