package io.pleo.antaeus.core.services.billing.task.pending

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PendingBillingTaskTest {

    private val failureInvoice = Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PENDING)

    private var customerService = mockk<CustomerService> {
    }

    private var invoiceService = mockk<InvoiceService> {
    }

    private var paymentProvider = mockk<PaymentProvider> {
    }

    private var failureBillingTask = PendingBillingTask(customerService, invoiceService, paymentProvider)

    @Test
    fun `assert zero calls if failures queue is empty`() {

        invoiceService = mockk {
            every { fetchByStatus(any()) } returns emptyList()
        }

        failureBillingTask = PendingBillingTask(customerService, invoiceService, paymentProvider)

        failureBillingTask.run()


        verify(exactly = 1) { invoiceService.fetchByStatus(InvoiceStatus.PENDING)}
    }

    @Test
    fun `when charge is correct updates invoice as PAID`() {

        val slotInvoice = slot<Invoice>()

        invoiceService = mockk {
            every { fetchByStatus(any()) } returns listOf(failureInvoice)
            every { update(capture(slotInvoice)) } returns failureInvoice
        }

        paymentProvider = mockk {
            every { charge(any()) } returns true
        }

        failureBillingTask = PendingBillingTask(customerService, invoiceService, paymentProvider)

        failureBillingTask.run()

        Assertions.assertEquals(InvoiceStatus.PAID, slotInvoice.captured.status)
    }
}