# Solution

## Steps

### Step 1
- Because of my lack of specific knowledge in Kotlin, read several documentations and API description about the language.
- Also look for best practises, and further details regarding the language.

### Step 2
- Start adding a new configuration module for Platform Conf Properties injection
- Add new endpoint for retrieve invoices by status

### Step 3
- Start thinking how to solve the main exposed problem for the invoices 
- Decisions taken:
    - Schedule a Task to be run every 1st of Month.
        - this task will look for all invoices with PENDING status.
        - For each PENDING invoice, try to charge the invoice to the customer:
            - if charge operation is successful :
                - update the invoice as PAID in DB
            - If charge operation is false -> Means the customer has no balance
                - so proceed to set this customer as ACTIVE false in our DB
                    - This decision could be different, by trying that invoice charge more times, would be an appropiate soution too.
                    - But setting the customer as NOT ACTIVE, and notifying him, would allow the customer to act uppon his criteria.
                    - Until customer is again ACTIVE, the customer would no have access to our Company business services. 
                - Also send an email to the customer to notify this new status
            - If charge operation throws CustomerNotFoundException or CurrencyMismatchException
                - Means we have inconsistent data in our DB, because of customer ID or customer currency VS invoice currency.
                    - So send an email to our billing department to check it and fix this data.
                    - Invoice remains PENDING in our DB
            - If charge operation throws NetworkException
                - Retry that invoice charge until a max of 3 times.
                - If within the retries the charge is successful:
                    - update the invoice as PAID in DB
                - If after the retries the charge operation remains being FALSE
                    - Add this invoice to an Observer memory class (FailureInvoiceObserver), as FAILURE invoice.
                        - This could be achieved also by adding a new InvoiceStatus as FAILURE, and updating it in our DB
            - If any other exception happens in this logic process:
                - Sends an email to our IT department and Billing department to notify it and fix it.
                - Invoice remains PENDING in our DB
        - Once all the PENDING invoices in database are processed:
            - Check in the Observer memory class (FailureInvoiceObserver) if there are FAILURE invoices in the Queue
                - If there are:
                    - Schedule a single Task run with a delay of 3h to process each FAILURE invoice.
                    - The FAILURE invoices are the ones which after 4 attempts, weren't able to be charged.
                    - For each FAILURE invoice, proceed to try to charge it.
                    - Poll the Failure invoice from the Failures Queue.
                    - If the charge keeps failing:
                        - In this scenario there are no Retries logic.
                        - So send an email to our IT & billing department to check it and fix this data.
                        - Invoice remains PENDING in our DB
                    - If charge operation is successful:
                        - update the invoice as PAID in DB
                    

### Step 4
- IMHO there are some fields missing in some tables/entities:
    - Invoices
        - Date of the invoice --> [NOT ADDED] -> but i think its important for an invoice, identify to which dates it belongs
    - Customers
        - Active or not active --> [ADDED] -> a customer can be active for our business, or not. If some invoice charge problems happen
        - the Timezone of the customer --> [NOT ADDED] -> If a customer is from a country with different timezones, it should be taken into account when charge him an invoice.

## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will pay those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

### Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|
|       Packages containing the main() application. 
|       This is where all the dependencies are instantiated.
|
├── pleo-antaeus-core
|
|       This is where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|
|       Module interfacing with the database. Contains the models, mappings and access layer.
|
├── pleo-antaeus-models
|
|       Definition of models used throughout the application.
|
├── pleo-antaeus-rest
|
|        Entry point for REST API. This is where the routes are defined.
└──
```

## Instructions
Fork this repo with your solution. We want to see your progression through commits (don’t commit the entire solution in 1 step) and don't forget to create a README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

Happy hacking 😁!

## How to run
```
./docker-start.sh
```

## Libraries currently in use
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
