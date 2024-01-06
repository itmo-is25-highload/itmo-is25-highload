package ru.itmo.ratelimit.client.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import ru.itmo.common.configuration.properties.YamlPropertySourceFactory

@PropertySource("classpath:rate-limiter-client-properties.yaml", factory = YamlPropertySourceFactory::class)
@ConfigurationProperties(prefix = "services.rate-limiter-endpoint")
@Component
class RateLimiterClientProperties { lateinit var url: String }
