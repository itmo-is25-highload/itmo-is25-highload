package ru.itmo.storage.storage.compression.properties

import org.springframework.boot.context.properties.ConfigurationProperties
@ConfigurationProperties("storage.component.compression")
class DeflateCompressionServiceProperties(
    val allocatedBufferSize: Int,
)
