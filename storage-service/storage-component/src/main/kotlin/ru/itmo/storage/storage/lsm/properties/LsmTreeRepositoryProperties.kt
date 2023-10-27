package ru.itmo.storage.storage.lsm.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.memtable")
class LsmTreeRepositoryProperties {
    var maxSize: Long = 4096
}
