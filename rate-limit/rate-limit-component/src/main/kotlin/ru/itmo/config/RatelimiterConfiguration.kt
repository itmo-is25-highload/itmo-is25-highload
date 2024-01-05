package ru.itmo.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import ru.itmo.ratelimit.component.properties.RatelimiterPropertiesEntry

@EnableConfigurationProperties(RatelimiterConfiguration::class)
@ConfigurationProperties("descriptors")
class RatelimiterConfiguration {
    lateinit var properties: List<RatelimiterPropertiesEntry>
}
