package io.pleo.antaeus.core.services.billing.task.failure

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.pleo.antaeus.core.external.MailSender
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class FailureBillingTaskHandlerTest {

    private val customer = Customer(1, Currency.EUR, true)
    private val failureInvoice = Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PENDING)

    private var customerService = mockk<CustomerService> {
    }
    private var paymentProvider = mockk<PaymentProvider> {
    }
    private var mailSender = mockk<MailSender> (relaxUnitFun = true)

    private var failureBillingTaskHandler = FailureBillingTaskHandler(customerService, paymentProvider, mailSender)

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

        failureBillingTaskHandler = FailureBillingTaskHandler(customerService, paymentProvider, mailSender)

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

        failureBillingTaskHandler = FailureBillingTaskHandler(customerService, paymentProvider, mailSender)

        failureBillingTaskHandler.handle(failureInvoice)

        verify(exactly = 0) { customerService.update(any())}
        verify(exactly = 0) { mailSender.send(any())}
    }

    @Test
    fun `assert when Exception dont try Retries and sends mail`() {

        paymentProvider = mockk {
            every { charge(any()) } throws Exception()
        }

        failureBillingTaskHandler = FailureBillingTaskHandler(customerService, paymentProvider, mailSender)

        failureBillingTaskHandler.handle(failureInvoice)

        verify(exactly = 1) { paymentProvider.charge(any())}
        verify(exactly = 1) { mailSender.send(any())}

        Assertions.assertFalse(FailureInvoiceObserver.instance.hasFailures())
    }

}