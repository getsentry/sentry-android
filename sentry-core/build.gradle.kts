plugins {
    java
    kotlin("jvm")
    jacoco
    `maven-publish`
}

dependencies {
    // tests
    testImplementation(kotlin(Config.kotlinStdLib))
    testImplementation(Config.TestLibs.kotlinTestJunit)
    testImplementation(Config.TestLibs.mockitoKotlin)
}

configure<SourceSetContainer> {
    test {
        java.srcDir("src/test/java")
    }
}

jacoco {
    toolVersion = Config.QualityPlugins.jacocoVersion
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

publishing {
    publications {

        // Publish the release aar artifact
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            groupId = "${project.group}"
            version = "${project.version}"
        }
    }
}
