package ru.itmo.storage.storage.jobs.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage.component.jobs.merge-ss-table")
data class MergeSstableJobProperties(
    val cron: String,
)
