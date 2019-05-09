package io.pleo.antaeus.core.services.billing.task.pending

import com.github.shyiko.skedule.Schedule
import io.pleo.antaeus.app.Configuration
import io.pleo.antaeus.app.DomainConfig
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PendingBillingTaskScheduler(
        private val pendingBillingTask: PendingBillingTask) {

    private val logger = KotlinLogging.logger {}

    private val executor = Executors.newSingleThreadScheduledExecutor()

    /*
     * this task runs periodically on 1st day of each month for process all PENDING invoices
     */
    fun schedulePendingInvoices(): ScheduledFuture<*>? {

        val now = ZonedDateTime.now()
        val billingScheduleDelayExpression = Configuration.config[DomainConfig.billingScheduleDelayExpression]

        logger.info { "scheduled PendingBillingTaskScheduler at $now with expression $billingScheduleDelayExpression" }

        return executor.scheduleWithFixedDelay(
                pendingBillingTask,
                Schedule.parse(billingScheduleDelayExpression).next(now).toEpochSecond() - now.toEpochSecond(),
                Schedule.parse(billingScheduleDelayExpression).next(now).toEpochSecond() - now.toEpochSecond(),
        TimeUnit.SECONDS)
    }
}