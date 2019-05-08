package io.pleo.antaeus.core.services.billing.task.failure

import io.pleo.antaeus.core.external.MailSender
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.billing.task.BillingTaskHandler
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

/*
 * Handler for the Pending Tasks Executions.
 */
class FailureBillingTaskHandler(customerService: CustomerService,
                                paymentProvider: PaymentProvider,
                                mailSender: MailSender) : BillingTaskHandler(customerService, paymentProvider, mailSender) {

    private val logger = KotlinLogging.logger {}

    override fun handleException(ex: Exception, invoice: Invoice): Boolean {

        /*
         * Occurred exceptions for failure invoices --> log them and send an email to be further investigated.
         * If this happens means, this invoice failed when was initially PENDING, also its retries, and now treated as Failure.
         * NO retry failures.
         */
        logger.error(ex) {"Error trying to charge FAILURE invoice ${invoice.id} for customer ${invoice.customerId}. Proceed to send an email." }
        mailSender.send(invoice)
        return false
    }
}