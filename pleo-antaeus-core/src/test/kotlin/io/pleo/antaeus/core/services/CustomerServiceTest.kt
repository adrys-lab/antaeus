package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class CustomerServiceTest {

    private val customer = Customer(1, Currency.EUR, true)

    private val dal = mockk<AntaeusDal> {
        every { fetchCustomer(404) } returns null
        every { updateCustomer(any()) } returns customer
    }

    private val customerService = CustomerService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<CustomerNotFoundException> {
            customerService.fetch(404)
        }
    }

    @Test
    fun `returns well update call`() {
        val newCustomer = Customer(2, Currency.SEK, false)
        Assertions.assertEquals(customer, customerService.update(newCustomer))
    }
}