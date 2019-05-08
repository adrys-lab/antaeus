package io.pleo.antaeus.core.services.billing.task.pending

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.MailSender
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.billing.task.failure.FailureInvoiceObserver
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PendingBillingTaskHandlerTest {

    private val customer = Customer(1, Currency.EUR, true)
    private val failureInvoice = Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PENDING)

    private var customerService = mockk<CustomerService> {
    }
    private var paymentProvider = mockk<PaymentProvider> {
    }
    private var mailSender = mockk<MailSender> (relaxUnitFun = true)

    private var failureBillingTaskHandler = PendingBillingTaskHandler(customerService, paymentProvider, mailSender)

    @Test
    fun `assert charge result as false updates customer as INACTIVE and sends a mail`() {

        val slotCustomer = slot<Customer>()

        paymentProvider = mockk {
            every { charge(any()) } returns false
        }

        customerService = mockk {
            every { fetch(any()) } returns customer
            every { update(capture(slotCustomer)) } returns customer
        }

        failureBillingTaskHandler = PendingBillingTaskHandler(customerService, paymentProvider, mailSender)

        failureBillingTaskHandler.handle(failureInvoice)

        verify(exactly = 1) { customerService.update(any())}
        verify(exactly = 1) { mailSender.send(any())}

        Assertions.assertEquals(false, slotCustomer.captured.active)
    }

    @Test
    fun `assert charge result as true no call customer service nor mail`() {

        paymentProvider = mockk {
            every { charge(any()) } returns true
        }

        failureBillingTaskHandler = PendingBillingTaskHandler(customerService, paymentProvider, mailSender)

        failureBillingTaskHandler.handle(failureInvoice)

        verify(exactly = 0) { customerService.update(any())}
        verify(exactly = 0) { mailSender.send(any())}
    }

    @Test
    fun `assert when CustomerNotFoundException sends mail`() {

        paymentProvider = mockk {
            every { charge(any()) } throws  CustomerNotFoundException(1)
        }

        failureBillingTaskHandler = PendingBillingTaskHandler(customerService, paymentProvider, mailSender)

        failureBillingTaskHandler.handle(failureInvoice)

        verify(exactly = 1) { paymentProvider.charge(any())}
        verify(exactly = 0) { customerService.update(any())}
        verify(exactly = 1) { mailSender.send(any())}
    }

    @Test
    fun `assert when CurrencyMismatchException sends mail`() {

        paymentProvider = mockk {
            every { charge(any()) } throws  CurrencyMismatchException(1, 1)
        }

        failureBillingTaskHandler = PendingBillingTaskHandler(customerService, paymentProvider, mailSender)

        failureBillingTaskHandler.handle(failureInvoice)

        verify(exactly = 1) { paymentProvider.charge(any())}
        verify(exactly = 0) { customerService.update(any())}
        verify(exactly = 1) { mailSender.send(any())}
    }

    @Test
    fun `assert when NetworkException tries Retries and saves failure in the queue`() {

        paymentProvider = mockk {
            every { charge(any()) } throws NetworkException()
        }

        failureBillingTaskHandler = PendingBillingTaskHandler(customerService, paymentProvider, mailSender)

        failureBillingTaskHandler.handle(failureInvoice)

        verify(exactly = 4) { paymentProvider.charge(any())}

        Assertions.assertTrue(FailureInvoiceObserver.instance.hasFailures())
        Assertions.assertEquals(failureInvoice, FailureInvoiceObserver.instance.poll())
    }

}