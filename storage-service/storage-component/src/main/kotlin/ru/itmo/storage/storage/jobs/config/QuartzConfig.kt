package ru.itmo.storage.storage.jobs.config

import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.spi.TriggerFiredBundle
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import ru.itmo.storage.storage.jobs.properties.MergeSstableJobProperties
import ru.itmo.storage.storage.jobs.service.MergeSsTableJob

@Configuration
@Import(MergeSsTableJob::class)
@EnableConfigurationProperties(MergeSstableJobProperties::class)
class QuartzConfig(
    private val applicationContext: ApplicationContext,
    private val mergeSstableJobProperties: MergeSstableJobProperties,
) {

    @Bean
    fun showTimeJobDetail(): JobDetail = JobBuilder
        .newJob(MergeSsTableJob::class.java)
        .withIdentity("KEKJob")
        .storeDurably()
        .requestRecovery(true)
        .build()

    @Bean
    fun showTimeTrigger(jobDetail: JobDetail): Trigger = TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withIdentity("SimpleJobTrigger", "Utility")
        .withSchedule(CronScheduleBuilder.cronSchedule(mergeSstableJobProperties.cron))
        .build()

    @Bean
    fun createSpringBeanJobFactory(): SpringBeanJobFactory {
        return object : SpringBeanJobFactory() {
            @Throws(Exception::class)
            override fun createJobInstance(bundle: TriggerFiredBundle): Any {
                val job = super.createJobInstance(bundle)
                applicationContext
                    .autowireCapableBeanFactory
                    .autowireBean(job)
                return job
            }
        }
    }

    @Bean
    @Throws(SchedulerException::class)
    fun scheduler(trigger: Trigger, job: JobDetail, factory: SchedulerFactoryBean): Scheduler {
        val scheduler: Scheduler = factory.scheduler
        if (scheduler.checkExists(job.key)) {
            scheduler.deleteJob(job.key)
        }
        scheduler.scheduleJob(job, trigger)
        scheduler.start()
        return scheduler
    }
}
