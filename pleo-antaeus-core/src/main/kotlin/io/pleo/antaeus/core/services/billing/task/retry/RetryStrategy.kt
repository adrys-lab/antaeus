package io.pleo.antaeus.core.services.billing.task.retry

import io.pleo.antaeus.models.Invoice

interface RetryStrategy {

    fun retry(executionTime: Int, function: (Invoice) -> Boolean, invoice: Invoice): Boolean
}
