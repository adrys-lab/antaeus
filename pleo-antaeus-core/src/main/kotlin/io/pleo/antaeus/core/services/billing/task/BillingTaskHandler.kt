package io.pleo.antaeus.core.services.billing.task

import io.pleo.antaeus.core.external.MailSender
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

/*
 * Abstract class inherited by the Billing Task Handlers -> Pending & Failure
 */
abstract class BillingTaskHandler(
        private val customerService: CustomerService,
        private val paymentProvider: PaymentProvider,
        val mailSender: MailSender) {

    private val logger = KotlinLogging.logger {}

    fun handle(invoice: Invoice): Boolean {

        try {
            return tryCharge(invoice)
        } catch (ex: Exception) {
            return handleException(ex, invoice)
        }
    }

    /*
     * executes payment provider charge + log result.
     * If some exception is thrown, it will be handled and treated in the caller method.
     */
    fun tryCharge(invoice: Invoice): Boolean {

        val result = paymentProvider.charge(invoice)

        if(!result) {

            //This means the customer has not enough balance, meaning, customer can not pay, so proceed to save customer as NOT ACTIVE.
            logger.info{"Payment provider charge result is FALSE for invoice ${invoice.id}, so customer has not enough funds. Proceed to update customer ${invoice.customerId} as NOT ACTIVE." }
            val customer = customerService.fetch(invoice.customerId)
            val notActiveCustomer = Customer(customer.id, customer.currency, false)
            customerService.update(notActiveCustomer)

            //After the customer has been set as NOT ACTIVE, proceed to notify the him with that new state, via MAIL.
            mailSender.send(invoice)
        } else {
            logger.info{"Successfully charged invoice ${invoice.id}, with amount ${invoice.amount.value}  ${invoice.amount.currency} for customer ${invoice.customerId}." }
        }
        return result
    }

    //Each extended class decides what to do with the given exceptions
    abstract fun handleException(ex: Exception, invoice: Invoice): Boolean
}

internal fun getMailSender(): MailSender {
    return object : MailSender {
        override fun send(invoice: Invoice) {
        }
    }
}