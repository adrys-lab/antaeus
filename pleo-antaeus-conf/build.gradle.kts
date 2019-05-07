plugins {
    application
    kotlin("jvm")
}

kotlinProject()

dataLibs()


dependencies {
    compile("com.uchuhimo:konf:0.13.2")
}