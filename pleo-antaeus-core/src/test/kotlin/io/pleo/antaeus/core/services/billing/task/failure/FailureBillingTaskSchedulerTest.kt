package io.pleo.antaeus.core.services.billing.task.failure

import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit


class FailureBillingTaskSchedulerTest {

    private val failureBillingTask = mockk<FailureBillingTask> {}

    private val billingWrapper = FailureBillingTaskScheduler(failureBillingTask)

    @Test
    fun `test scheduling delay until next 1st`() {

        val schedule = billingWrapper.scheduleFailureInvoices()

        val scheduleDelay = schedule?.getDelay(TimeUnit.HOURS)?.plus(1)?.toInt()

        Assertions.assertEquals(3, scheduleDelay)
    }
}