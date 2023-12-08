package ru.itmo.storage.storage.config

import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.itmo.storage.storage.lsm.core.AVLTree

const val MEMTABLE_FLUSH = "memtable-flush"

@Configuration
class CoroutinesConfiguration {

    @Bean
    @Qualifier(MEMTABLE_FLUSH)
    fun memtableFlushFlow(): MutableSharedFlow<AVLTree> = MutableSharedFlow()
}
