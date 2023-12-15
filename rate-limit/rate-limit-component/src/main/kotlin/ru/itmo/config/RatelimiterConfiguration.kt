package ru.itmo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.itmo.properties.RatelimiterPropertiesEntry

@ConfigurationProperties("descriptors")
class RatelimiterConfiguration {
    lateinit var properties: List<RatelimiterPropertiesEntry>
}