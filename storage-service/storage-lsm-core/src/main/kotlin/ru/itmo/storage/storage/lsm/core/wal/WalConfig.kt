package ru.itmo.storage.storage.lsm.core.wal

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import ru.itmo.storage.storage.lsm.core.wal.repository.WalLogReadRepository

@Configuration
@Import(
    WalLoggingAspect::class,
    WalLogReadRepository::class,
)
class WalConfig
