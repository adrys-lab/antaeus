package io.pleo.antaeus.core.services.billing.task.retry

import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RetryStrategyWithMaxTest {

    private val invoice = Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PAID)
    private val retryStrategyWithMax = RetryStrategyWithMax()

    @Test
    fun `check Max retries`() {
        val retiesMock = RetriesFunctionMock(max = 3)
        retryStrategyWithMax.retry(0, { retiesMock.mock(invoice)}, invoice )
        Assertions.assertEquals(3, retiesMock.times)
    }

    @Test
    fun `Retries continue if any exception is thrown`() {
        val retiesMock = RetriesException(max = 3)
        retryStrategyWithMax.retry(0, { retiesMock.mock(invoice)}, invoice )
        Assertions.assertEquals(3, retiesMock.times)
    }

    private class RetriesFunctionMock(var times: Int = 0, val max: Int) {
        fun mock(invoice: Invoice): Boolean {
            times = times.plus(1)
            return times > max
        }
    }

    private class RetriesException(var times: Int = 0, val max: Int) {
        fun mock(invoice: Invoice): Boolean {
            times = times.plus(1)
            throw NetworkException()
        }
    }

}