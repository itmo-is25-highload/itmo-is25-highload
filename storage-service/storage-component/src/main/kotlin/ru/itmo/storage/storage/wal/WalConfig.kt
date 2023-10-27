package ru.itmo.storage.storage.wal

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.wal.repository.WalLogReadRepository

@Configuration
@Import(
    WalLoggingAspect::class,
    WalLogReadRepository::class,
)
class WalConfig
