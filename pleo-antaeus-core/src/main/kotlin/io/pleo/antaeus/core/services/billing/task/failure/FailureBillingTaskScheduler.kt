package io.pleo.antaeus.core.services.billing.task.failure

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

        val executor = Executors.newSingleThreadScheduledExecutor()

        return executor.schedule(
                failureBillingTask,
                TimeUnit.HOURS.toMillis(3L),
                TimeUnit.MILLISECONDS)
    }
}