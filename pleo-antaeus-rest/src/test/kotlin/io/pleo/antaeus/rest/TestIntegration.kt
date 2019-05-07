package io.pleo.antaeus.rest

import deserialize
import getPaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.models.Customer
import junit.framework.TestCase
import khttp.get
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import setupInitialData
import java.sql.Connection

class TestIntegration : TestCase() {

    private val url = "http://localhost:7000"

    companion object {
        private val tables = arrayOf(InvoiceTable, CustomerTable)

        // connect TEST database
        private val db = Database
                .connect("jdbc:sqlite:/tmp/testdata.db", "org.sqlite.JDBC")
                .also {
                    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                    transaction(it) {
                        addLogger(StdOutSqlLogger)
                        SchemaUtils.drop(*tables)
                        SchemaUtils.create(*tables)
                    }
                }

        private val antaeusDal = AntaeusDal(db)

        val invoiceService = InvoiceService(dal = antaeusDal)
        val customerService = CustomerService(dal = antaeusDal)
        val billingService = BillingService(paymentProvider = getPaymentProvider())

        var app = AntaeusRest(
                invoiceService = invoiceService,
                customerService = customerService
        )
    }

    override fun setUp() {

        transaction(db) {
            SchemaUtils.create(*tables)
        }

        setupInitialData(antaeusDal)

        app = AntaeusRest(
                invoiceService = invoiceService,
                customerService = customerService
        )
        app.run()
    }

    override fun tearDown() {
        transaction(db) {
            SchemaUtils.drop(*tables)
        }

        app.stop()
    }

    fun testHealthApi() {
        val response = get(url = url + "/rest/health")
        assertEquals(200, response.statusCode)
        assertEquals("ok", response.text.deserialize())
    }

    fun testFiveCustomers() {
        val customers = get(url = url + "/rest/v1/customers").text.deserialize<List<Customer>>()
        assertEquals(5, customers.size)
    }
}