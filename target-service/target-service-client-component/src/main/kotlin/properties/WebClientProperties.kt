package ru.itmo.target.client.properties

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import ru.itmo.target.properties.ClientProperties

@Configuration
class WebClientProperties {
    @Bean
    fun webClient(builder: WebClient.Builder, properties: ClientProperties): WebClient {
        return builder.baseUrl(properties.url).build()
    }
}
