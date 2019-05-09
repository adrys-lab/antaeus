package io.pleo.antaeus.core.services.billing.task.failure

import io.pleo.antaeus.models.Invoice
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/*
 * Observer pattern that holds failure invoices in a Queue to be post-processed.
 * Private constructor to enclose its construction by singleton pattern.
 * failureInvoices --> ConcurrentLinkedQueue is appropriate if several threads shares access to a common collection
 */
class FailureInvoiceObserver private constructor(
        private val failureInvoices : Queue<Invoice> = ConcurrentLinkedQueue()) {

    //Singleton instance
    companion object {
        val instance = FailureInvoiceObserver()
    }

    //Use observer common method notify to add a new invoice to the Queue.
    fun notify(invoice: Invoice) {
        failureInvoices.add(invoice)
    }

    //only way to access failure invoices is through Poll Queue method.
    fun poll(): Invoice {
        return failureInvoices.poll()
    }

    fun hasFailures(): Boolean {
        return failureInvoices.isNotEmpty()
    }
}