import com.jfrog.bintray.gradle.tasks.BintrayPublishTask
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.publish.PublishingExtension

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm")
    jacoco
    id("net.ltgt.errorprone")
    id(Config.PublishPlugins.bintrayPlugin)
}

dependencies {
    // Envelopes require JSON. Until a parse is done without GSON, we'll depend on it explicitly here
    implementation(Config.Libs.gson)

    compileOnly(Config.CompileOnly.noopen)
    errorprone(Config.CompileOnly.noopenProne)
    errorprone(Config.CompileOnly.errorprone)
    errorproneJavac(Config.CompileOnly.errorProneJavac)
    compileOnly(Config.CompileOnly.annotations)

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

    withType<BintrayUploadTask> {
        bintray {
            user = System.getenv("BINTRAY_USER")
            key = System.getenv("BINTRAY_KEY")
            with(pkg) {
                repo = "mvn"
                name = project.name
                userOrg = "getsentry"
                setLicenses("MIT")
                websiteUrl = "https://sentry.io"
                vcsUrl = "https://github.com/getsentry/sentry-android"
                setLabels("sentry", "getsentry", "error-tracking", "crash-reporting")
                with(version) {
                    name = project.version.toString()
                    vcsTag = project.version.toString()
                    desc = project.description.toString()
                }
            }
            println("version: $version")
            setPublications(project.name)
        }
    }
}

publishing {
    publications {
        register(project.name, MavenPublication::class) {
            if (project.hasProperty("android")) {
                artifact("$buildDir/outputs/aar/${project.name}-release.aar")
            } else {
                from(components["java"])
            }
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
//            artifact(sourcesJar)
//            artifact(javadocJar)

            if (project.hasProperty("android")) {
                pom {
                    withXml {
                        asNode().appendNode("dependencies").let { depNode ->
                            configurations.implementation.get().allDependencies.forEach {
                                depNode.appendNode("dependency").apply {
                                    appendNode("groupId", it.group)
                                    appendNode("artifactId", it.name)
                                    appendNode("version", it.version)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
