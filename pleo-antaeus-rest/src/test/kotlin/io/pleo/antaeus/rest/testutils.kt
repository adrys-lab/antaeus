import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.math.BigDecimal
import kotlin.random.Random

inline fun <reified T : Any> String.deserialize(): T =
        jacksonObjectMapper().readValue(this)

internal fun addFivePendingInvoices(dal: AntaeusDal) {
    (1..5).forEach {
        dal.createCustomer(
                currency = Currency.values()[Random.nextInt(0, Currency.values().size)],
                active = true
        )?.let { customer ->
            dal.createInvoice(
                amount = Money(
                        value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                        currency = Currency.EUR
                ),
                customer = customer,
                status = InvoiceStatus.PENDING
            )
        }
    }
}

internal fun setupInitialData(dal: AntaeusDal) {

    val customers = (1..5).mapNotNull {
        dal.createCustomer(
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)],
            active = true
        )
    }

    customers.forEach { customer ->
        (1..10).forEach {
            dal.createInvoice(
                amount = Money(
                    value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                    currency = customer.currency
                ),
                customer = customer,
                status = InvoiceStatus.PAID
            )
        }
    }
}

// This is the mocked instance of the payment provider
fun getPaymentProvider(): PaymentProvider {
    return object : PaymentProvider {
        override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
        }
    }
}