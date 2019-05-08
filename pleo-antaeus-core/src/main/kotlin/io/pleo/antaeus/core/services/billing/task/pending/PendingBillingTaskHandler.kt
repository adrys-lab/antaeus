package io.pleo.antaeus.core.services.billing.task.pending

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.MailSender
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.billing.task.BillingTaskHandler
import io.pleo.antaeus.core.services.billing.task.failure.FailureInvoiceObserver
import io.pleo.antaeus.core.services.billing.task.retry.RetryStrategyWithMax
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

/*
 * Handler for the Pending Tasks Executions.
 */
class PendingBillingTaskHandler(customerService: CustomerService,
                                paymentProvider: PaymentProvider,
                                mailSender: MailSender) : BillingTaskHandler(customerService, paymentProvider, mailSender) {

    private val logger = KotlinLogging.logger {}

    override fun handleException(ex: Exception, invoice: Invoice): Boolean {

        when(ex) {
            is CustomerNotFoundException, is CurrencyMismatchException -> {
                // for currency or customers exceptions, a post-action is needed, so proceed to log the error, and send an email for investigate and take action.
                logger.error(ex) {"Business logic error trying to charge invoice ${invoice.id} for customer ${invoice.customerId}" }
                mailSender.send(invoice)
            }
            is NetworkException -> {
                /*
                 * for network exceptions for pending invoices:
                 * - Retry the execution with a MAX of 3 times.
                 * - If after retries, its still failure, add the invoice to the Failure Observer to be treated afterwards as FAILURE.
                 * - If after retries, is successful, return true as valid invoice charge.
                 */
                logger.info(ex) {"Network exception for invoice ${invoice.id} for customer ${invoice.customerId}. Proceed to retry its charging." }
                val retryResult = RetryStrategyWithMax().retry(0, { tryCharge(invoice) }, invoice)
                if(!retryResult) {
                    logger.info(ex) {"After retries for invoice ${invoice.id} result keeps being failure, so proceed to add it to the Failure Queue." }
                    FailureInvoiceObserver.instance.notify(invoice)
                } else {
                    return true
                }
            }
            else -> {
                // some unexpected error happened, this needs further investigations so proceed to log the error and send an email
                logger.error(ex) {"Critical exception for invoice ${invoice.id} for customer ${invoice.customerId}." }
                mailSender.send(invoice)
            }
        }

        return false
    }
}