package io.pleo.antaeus.core.services.billing.task.pending

import com.github.shyiko.skedule.Schedule
import io.pleo.antaeus.app.Configuration
import io.pleo.antaeus.app.DomainConfig
import io.pleo.antaeus.app.ServerConfig
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class PendingBillingTaskScheduler(
        private val pendingBillingTask: PendingBillingTask) {

    private val logger = KotlinLogging.logger {}
    private val executor = ScheduledThreadPoolExecutor(Configuration.config[ServerConfig.executorThreadPool])

    // initializer block
    init {
        executor.removeOnCancelPolicy = true
    }

    //this task runs on 1st day of each month for process all PENDING invoices

    fun schedulePendingInvoices(): ScheduledFuture<*>? {

        val now = ZonedDateTime.now()
        val billingScheduleDelayExpression = Configuration.config[DomainConfig.billingScheduleDelayExpression]

        logger.info { "scheduled PendingBillingTaskScheduler at $now with expression $billingScheduleDelayExpression" }

        return executor.schedule(
                pendingBillingTask,
                Schedule.parse(billingScheduleDelayExpression).next(now).toEpochSecond() - now.toEpochSecond(),
        TimeUnit.SECONDS)
    }
}