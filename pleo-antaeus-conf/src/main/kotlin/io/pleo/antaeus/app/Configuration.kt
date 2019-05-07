package io.pleo.antaeus.app

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec

//Configurations regarding Server environment.
object ServerConfig : ConfigSpec("server") {
    val port by required<Int>()
    val executorThreadPool by required<Int>()
}

//Configurations regarding Business logic and Domain specific.
object DomainConfig : ConfigSpec("domain") {
    val fromMail by required<String>()
    val failureMail by required<String>()
    val failureMaxRetries by required<Int>()
    val billingScheduleDelayExpression by required<String>()
}

object Configuration {
    val config = Config {
                addSpec(ServerConfig)
                addSpec(DomainConfig)
            }
            .from.yaml.watchFile(this.javaClass.getResource("/configuration.yml").file)
            .from.env()
            .from.systemProperties()
}