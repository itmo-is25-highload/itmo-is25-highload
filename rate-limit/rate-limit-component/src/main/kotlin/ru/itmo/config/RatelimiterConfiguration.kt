package ru.itmo.ratelimit

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import ru.itmo.common.configuration.properties.YamlPropertySourceFactory
import ru.itmo.ratelimit.component.properties.RatelimiterPropertiesEntry

@Configuration
@PropertySource("classpath:rate-limiter-config.yaml", factory = YamlPropertySourceFactory::class)
@ConfigurationProperties(prefix = "limit")
class RatelimiterConfiguration {
    lateinit var descriptors: Collection<RatelimiterPropertiesEntry>
}
