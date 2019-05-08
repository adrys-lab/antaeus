package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.Invoice

//Mail sender is an interface that simulates an email to be sent.
interface MailSender {
    fun send(invoice: Invoice)
}
