package io.pleo.antaeus.core.services.billing.task.failure

import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class FailureBillingTask(
        private val invoiceService: InvoiceService,
        private val failureBillingTaskHandler: FailureBillingTaskHandler) : Runnable {

    private val logger = KotlinLogging.logger {}

    override fun run() {

        while(FailureInvoiceObserver.instance.hasFailures()) {

            val invoice = FailureInvoiceObserver.instance.poll()

            logger.info { "Proceed to pay FAILURE invoice ${invoice.id}" }

            val chargeResult = failureBillingTaskHandler.handle(invoice)

            if(chargeResult) {
                logger.info { "FAILURE Invoice ${invoice.id} has been successfully paid" }
                val newInvoice = Invoice(invoice.id, invoice.customerId, invoice.amount,InvoiceStatus.PAID)
                invoiceService.update(newInvoice)
            }
        }

    }

}