package io.pleo.antaeus.app

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec

//Configurations regarding Server environment.
object ServerConfig : ConfigSpec("server") {
    val port by required<Int>()
}

//Configurations regarding Business logic and Domain specific.
object DomainConfig : ConfigSpec("domain") {
    val invoiceMaxRetries by required<Int>()
    val billingScheduleDelayExpression by required<String>()
    val failureBillingScheduleDelayHours by required<Long>()
}

object Configuration {
    val config = Config {
                addSpec(ServerConfig)
                addSpec(DomainConfig)
            }
            .from.yaml.watchFile(Configuration::class.java.getResource("/configuration.yml").file)
            .from.env()
            .from.systemProperties()
}