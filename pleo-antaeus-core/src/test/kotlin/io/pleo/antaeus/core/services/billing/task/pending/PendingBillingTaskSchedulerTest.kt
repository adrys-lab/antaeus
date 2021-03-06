package io.pleo.antaeus.core.services.billing.task.pending

import io.mockk.mockk
import io.pleo.antaeus.core.services.billing.task.pending.PendingBillingTask
import io.pleo.antaeus.core.services.billing.task.pending.PendingBillingTaskScheduler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit


class PendingBillingTaskSchedulerTest {

    private val billingExecutionTask = mockk<PendingBillingTask> {}

    private val billingWrapper = PendingBillingTaskScheduler(billingExecutionTask)

    @Test
    fun `test scheduling delay until next 1st`() {

        val schedule = billingWrapper.schedulePendingInvoices()

        val currentTime = ZonedDateTime.now()

        val nextFirstMonth = TemporalAdjusters.firstDayOfNextMonth().adjustInto(currentTime)

        val scheduleDelay = schedule?.getDelay(TimeUnit.DAYS)?.plus(1L)

        Assertions.assertEquals(ChronoUnit.DAYS.between(currentTime, nextFirstMonth), scheduleDelay)
    }
}