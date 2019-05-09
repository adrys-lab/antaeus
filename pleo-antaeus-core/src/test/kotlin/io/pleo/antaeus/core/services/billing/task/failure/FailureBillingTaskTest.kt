package io.pleo.antaeus.core.services.billing.task.failure

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class FailureBillingTaskTest {

    private val failureInvoice = Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PENDING)

    private var invoiceService = mockk<InvoiceService> {
    }

    private var failureBillingTaskHandler = mockk<FailureBillingTaskHandler> {
    }

    private var failureBillingTask = FailureBillingTask(invoiceService, failureBillingTaskHandler)

    @Test
    fun `assert zero calls if failures queue is empty`() {
        failureBillingTask.run()
        verify(exactly = 0) { failureBillingTaskHandler.handle(any())}
    }

    @Test
    fun `assert correct invoice result updates invoice as PAID`() {

        val slotInvoice = slot<Invoice>()

        failureBillingTaskHandler = mockk {
            every { handle(any()) } returns true
        }

        invoiceService = mockk {
            every { update(capture(slotInvoice)) } returns failureInvoice
        }

        failureBillingTask = FailureBillingTask(invoiceService, failureBillingTaskHandler)

        FailureInvoiceObserver.instance.notify(failureInvoice)

        failureBillingTask.run()

        verify(exactly = 1) { invoiceService.update(any())}

        Assertions.assertEquals(InvoiceStatus.PAID, slotInvoice.captured.status)
    }

    @Test
    fun `assert handle is called with correct invoice`() {

        failureBillingTaskHandler = mockk {
            every { handle(any()) } returns false
        }

        failureBillingTask = FailureBillingTask(invoiceService, failureBillingTaskHandler)

        FailureInvoiceObserver.instance.notify(failureInvoice)

        failureBillingTask.run()
        verify(exactly = 1) { failureBillingTaskHandler.handle(failureInvoice)}
    }

    @Test
    fun `assert incorrect invoice dont update invoice as PAID`() {

        val slotInvoice = slot<Invoice>()

        failureBillingTaskHandler = mockk {
            every { handle(any()) } returns false
        }

        invoiceService = mockk {
            every { update(capture(slotInvoice)) } returns failureInvoice
        }

        failureBillingTask = FailureBillingTask(invoiceService, failureBillingTaskHandler)

        FailureInvoiceObserver.instance.notify(failureInvoice)

        failureBillingTask.run()

        verify(exactly = 0) { invoiceService.update(any())}
    }

    @Test
    fun `assert FAILURE queue is empty after process`() {

        val slotInvoice = slot<Invoice>()

        failureBillingTaskHandler = mockk {
            every { handle(any()) } returns true
        }

        invoiceService = mockk {
            every { update(capture(slotInvoice)) } returns failureInvoice
        }

        failureBillingTask = FailureBillingTask(invoiceService, failureBillingTaskHandler)

        FailureInvoiceObserver.instance.notify(failureInvoice)
        FailureInvoiceObserver.instance.notify(failureInvoice)
        FailureInvoiceObserver.instance.notify(failureInvoice)

        failureBillingTask.run()

        verify(exactly = 3) { failureBillingTaskHandler.handle(failureInvoice)}

        Assertions.assertFalse(FailureInvoiceObserver.instance.hasFailures())
    }

}