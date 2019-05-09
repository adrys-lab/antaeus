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

    private val executor = Executors.newSingleThreadScheduledExecutor()

    /*
     * Schedules a new task to be executed once, with a delay of 3h (by configuration file).
     * To process al failure invoices present in FailureInvoiceObserver.instance.failureInvoices
     */
    fun scheduleFailureInvoices(): ScheduledFuture<*>? {

        val failureBillingScheduleDelayHours = Configuration.config[DomainConfig.failureBillingScheduleDelayHours]

        logger.info { "Scheduled Failure Billing Task at ${ZonedDateTime.now() } with delay of $failureBillingScheduleDelayHours hours" }

        return executor.schedule(
                failureBillingTask,
                failureBillingScheduleDelayHours,
                TimeUnit.HOURS)
    }
}