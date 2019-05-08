plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))

    implementation(project(":pleo-antaeus-conf"))

    compile(project(":pleo-antaeus-models"))

    compile("com.github.shyiko.skedule:skedule:0.4.0")
}