plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("io.sentry.console.Main")
    mainModule.set("io.sentry.console-sample")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":sentry-core"))

    testImplementation(kotlin(Config.kotlinStdLib))
    testImplementation(Config.TestLibs.kotlinTestJunit)
    testImplementation(Config.TestLibs.mockitoKotlin)
    testImplementation(Config.TestLibs.awaitility)
}
