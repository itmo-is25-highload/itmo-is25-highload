package ru.itmo.storage.storage.lsm.core.bloomfilter

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.sstable.filter")
class BloomFilterProperties {
    var maxSize: Int = 100
}
