package ru.itmo.target.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties

@EnableConfigurationProperties(ClientProperties::class)
@ConfigurationProperties("services.target-service-endpoint")
class ClientProperties(var serviceUrl: String)
