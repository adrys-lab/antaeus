package io.pleo.antaeus.core.services.billing.task.failure

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class FailureInvoiceObserverTest {

    private val firstInvoice = Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PAID)
    private val secondInvoice = Invoice(2, 2, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PAID)

    @Test
    fun `assert has failures`() {
        FailureInvoiceObserver.instance.notify(firstInvoice)
        Assertions.assertTrue(FailureInvoiceObserver.instance.hasFailures())
        FailureInvoiceObserver.instance.poll()
        Assertions.assertFalse(FailureInvoiceObserver.instance.hasFailures())
    }

    @Test
    fun `assert well poll objects`() {
        FailureInvoiceObserver.instance.notify(firstInvoice)
        FailureInvoiceObserver.instance.notify(secondInvoice)

        Assertions.assertEquals(firstInvoice, FailureInvoiceObserver.instance.poll())
        Assertions.assertEquals(secondInvoice, FailureInvoiceObserver.instance.poll())
    }

    @Test
    fun `assert empty poll throws exception`() {
        assertThrows<IllegalStateException> {
            FailureInvoiceObserver.instance.poll()
        }
    }
}