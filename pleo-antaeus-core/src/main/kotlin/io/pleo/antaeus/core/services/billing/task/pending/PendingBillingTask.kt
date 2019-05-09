package io.pleo.antaeus.core.services.billing.task.pending

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.billing.task.failure.FailureInvoiceObserver
import io.pleo.antaeus.core.services.billing.task.failure.FailureBillingTask
import io.pleo.antaeus.core.services.billing.task.failure.FailureBillingTaskHandler
import io.pleo.antaeus.core.services.billing.task.failure.FailureBillingTaskScheduler
import io.pleo.antaeus.core.services.billing.task.getMailSender
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class PendingBillingTask(
        private val customerService: CustomerService,
        private val invoiceService: InvoiceService,
        private val paymentProvider: PaymentProvider,
        private val pendingBillingTaskHandler: PendingBillingTaskHandler = PendingBillingTaskHandler(customerService, paymentProvider, getMailSender())
    ) : Runnable {

    private val logger = KotlinLogging.logger {}

    override fun run() {

        invoiceService.fetchByStatus(InvoiceStatus.PENDING)
                .forEach{

                    logger.info { "Proceed to pay PENDING invoice ${it.id}" }

                    val chargeResult = pendingBillingTaskHandler.handle(it)

                    if(chargeResult) {
                        logger.info { "PENDING Invoice ${it.id} has been successfully paid" }
                        val invoice = Invoice(it.id, it.customerId, it.amount,InvoiceStatus.PAID)
                        invoiceService.update(invoice)
                    }
                }

        logger.info { "PENDING Invoices process finished successfully." }

        /*
         * After all PENDING invoices has been treated, check if some of them has been Failure.
         * Failure means some Network exception happened.
         */
        if(FailureInvoiceObserver.instance.hasFailures()) {
           val failureBillingTask = FailureBillingTask(invoiceService, FailureBillingTaskHandler(customerService, paymentProvider, getMailSender()))

           FailureBillingTaskScheduler(failureBillingTask)
                   .scheduleFailureInvoices()
        }
    }

}