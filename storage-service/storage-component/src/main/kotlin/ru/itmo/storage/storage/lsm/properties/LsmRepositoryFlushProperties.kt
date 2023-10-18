package ru.itmo.storage.storage.lsm.properties

import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.flush")
class LsmRepositoryFlushProperties(
    @NotBlank
    val tableParentDir: String,

    @NotBlank
    val keyValueDelimiter: String,

    val blockSizeInBytes: Long,

    @NotBlank
    val entryDelimiter: String,
)
