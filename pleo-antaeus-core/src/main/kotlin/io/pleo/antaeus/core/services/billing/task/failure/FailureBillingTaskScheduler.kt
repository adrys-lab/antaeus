package io.pleo.antaeus.core.services.billing.task.failure

import io.pleo.antaeus.app.Configuration
import io.pleo.antaeus.app.DomainConfig
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class FailureBillingTaskScheduler(
    private val failureBillingTask: FailureBillingTask) {

    private val logger = KotlinLogging.logger {}

    /*
     * Schedules a new task to be executed once, with a delay of 3h.
     */
    fun scheduleFailureInvoices(): ScheduledFuture<*>? {

        logger.info { "Scheduled Failure Billing Task at ${ZonedDateTime.now() } with delay of X hours" }

        val failureBillingScheduleDelayHours = Configuration.config[DomainConfig.failureBillingScheduleDelayHours]

        val executor = Executors.newSingleThreadScheduledExecutor()

        return executor.schedule(
                failureBillingTask,
                TimeUnit.HOURS.toMillis(failureBillingScheduleDelayHours),
                TimeUnit.MILLISECONDS)
    }
}