package io.pleo.antaeus.models

data class Customer(
    val id: Int,
    val currency: Currency,

    /*
     * Indicates if this customer is active or not.
     * For business logic, we will be able to check if customer is active to allow him to do certain operations.
     */
    val active: Boolean
)
