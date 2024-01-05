package ru.itmo.ratelimit.client.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties

@EnableConfigurationProperties(RateLimiterClientProperties::class)
@ConfigurationProperties("services.rate-limiter-endpoint")
data class RateLimiterClientProperties(val Url: String)