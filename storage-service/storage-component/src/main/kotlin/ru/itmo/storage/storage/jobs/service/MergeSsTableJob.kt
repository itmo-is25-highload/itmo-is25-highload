package ru.itmo.storage.storage.jobs.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Service

@DisallowConcurrentExecution
@Service
class MergeSsTableJob : QuartzJobBean() {

    private val log = KotlinLogging.logger { }

    override fun executeInternal(context: JobExecutionContext) {
        log.info { "Job execution started" }
    }
}
