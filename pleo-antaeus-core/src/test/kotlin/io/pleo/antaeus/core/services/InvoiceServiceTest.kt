package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {

    private val pendingInvoices = listOf(Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PENDING))
    private val paidInvoices = listOf(Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PAID))

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { fetchInvoiceByStatus(InvoiceStatus.PENDING) } returns pendingInvoices
        every { fetchInvoiceByStatus(InvoiceStatus.PAID) } returns paidInvoices
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `return PENDING invoices`() {
        Assertions.assertEquals(pendingInvoices, invoiceService.fetchByStatus(InvoiceStatus.PENDING))
    }

    @Test
    fun `return PAID invoices`() {
        Assertions.assertEquals(paidInvoices, invoiceService.fetchByStatus(InvoiceStatus.PAID))
    }
}