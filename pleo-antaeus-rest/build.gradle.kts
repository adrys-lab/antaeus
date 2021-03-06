plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))

    implementation(project(":pleo-antaeus-core"))
    implementation(project(":pleo-antaeus-models"))
    implementation(project(":pleo-antaeus-conf"))

    implementation("io.javalin:javalin:2.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")

    testCompile("junit:junit:4.12")

    testImplementation("khttp:khttp:0.1.0")
    testImplementation("org.jetbrains.exposed:exposed:0.12.1")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
}
