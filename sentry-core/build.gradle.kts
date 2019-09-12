plugins {
    java
    kotlin("jvm") version "1.3.50"
    jacoco
}

dependencies {
    testImplementation(kotlin("stdlib"))
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.50")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.3.50")
}

jacoco {
    toolVersion = "0.8.4"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = false
    }
}

tasks {
    jacocoTestCoverageVerification {
        violationRules {
            // TODO: Raise the minimum to a sensible value.
            rule { limit { minimum = BigDecimal.valueOf(0.1) } }
        }
    }
    check {
        dependsOn(jacocoTestCoverageVerification)
        dependsOn(jacocoTestReport)
    }
}
