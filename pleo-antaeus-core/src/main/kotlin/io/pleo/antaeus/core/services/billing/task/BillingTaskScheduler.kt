package io.pleo.antaeus.core.services.billing.task

import com.github.shyiko.skedule.Schedule
import io.pleo.antaeus.app.Configuration
import io.pleo.antaeus.app.DomainConfig
import io.pleo.antaeus.app.ServerConfig
import io.pleo.antaeus.core.external.PaymentProvider
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class BillingTaskScheduler(
    private val paymentProvider: PaymentProvider,
    private val billingExecutionTask: BillingExecutionTask) {

    fun schedule(): ScheduledFuture<*>? {
        val executor = ScheduledThreadPoolExecutor(Configuration.config[ServerConfig.executorThreadPool])
        executor.removeOnCancelPolicy = true

        val now = ZonedDateTime.now()

        return executor.schedule(
                billingExecutionTask,
                Schedule.parse(Configuration.config[DomainConfig.billingScheduleDelayExpression]).next(now).toEpochSecond() - now.toEpochSecond(),
        TimeUnit.SECONDS)
    }
}