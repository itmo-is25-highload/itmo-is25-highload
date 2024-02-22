package ru.itmo.target.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component
import ru.itmo.common.configuration.properties.YamlPropertySourceFactory

@ConfigurationProperties(prefix = "services.target-service-endpoint")
@PropertySource("classpath:target-client-properties.yaml", factory = YamlPropertySourceFactory::class)
@Component
class ClientProperties { lateinit var url: String }
