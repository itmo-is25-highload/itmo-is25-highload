package configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("client.url")
class RateLimiterClientProperties(Url: String)