package properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("client")
class ClientProperties(var serviceUrl: String)
