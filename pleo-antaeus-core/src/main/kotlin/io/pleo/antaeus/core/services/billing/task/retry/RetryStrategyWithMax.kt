package io.pleo.antaeus.core.services.billing.task.retry

import io.pleo.antaeus.app.Configuration
import io.pleo.antaeus.app.DomainConfig
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

class RetryStrategyWithMax: RetryStrategy {

    private val logger = KotlinLogging.logger {}

    override fun retry(executionTime: Int, function: (Invoice) -> Boolean, invoice: Invoice): Boolean {

        logger.info{"Retry function execution for invoice ${invoice.id}, execution time $executionTime"}

        val maxRetries = Configuration.config[DomainConfig.invoiceMaxRetries]

        if(executionTime > maxRetries) {
            logger.warn{"Retries limit $maxRetries reached for invoice ${invoice.id}, result is FAILURE" }
            return false
        }

        val retryResult = function.invoke(invoice)

        if(!retryResult) {
            return retry(executionTime.plus(1), function, invoice )
        }

        logger.info{"Retry function execution for invoice ${invoice.id}, result is SUCCESSFUL after ${executionTime.plus(1) }  retries" }

        return true
    }
}