package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {

    private val pendingInvoices = listOf(Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PENDING))
    private val paidInvoices = listOf(Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PAID))

    private val invoice = Invoice(1, 1, Money(BigDecimal.ONE, Currency.EUR), InvoiceStatus.PAID)

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { fetchInvoiceByStatus(InvoiceStatus.PENDING) } returns pendingInvoices
        every { fetchInvoiceByStatus(InvoiceStatus.PAID) } returns paidInvoices
        every { updateInvoice(any()) } returns invoice
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

    @Test
    fun `returns well update call`() {

        val newInvoice = Invoice(10, 2, Money(BigDecimal.TEN, Currency.GBP), InvoiceStatus.PAID)

        Assertions.assertEquals(invoice, invoiceService.update(newInvoice))
    }
}