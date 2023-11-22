package ru.itmo.storage.storage.identifiers.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.identifiers.TimestampIdentifierService
import java.time.Clock

@Configuration
@Import(
    TimestampIdentifierService::class,
)
class UniqueIdentifiersConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
