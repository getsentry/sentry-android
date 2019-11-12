import com.novoda.gradle.release.PublishExtension

plugins {
    `java-library`
    kotlin("jvm")
    jacoco
    id("net.ltgt.errorprone")
    maven
    id(Config.Deploy.bintrayPlugin)
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
}

//TODO: move thse blocks to parent gradle file, DRY
configure<PublishExtension> {
    userOrg = Config.Sentry.userOrg
    groupId = project.group.toString()
    publishVersion = project.version.toString()
    desc = Config.Sentry.description
    website = Config.Sentry.website
    repoName = Config.Sentry.repoName
    setLicences(Config.Sentry.licence)
    issueTracker = Config.Sentry.issueTracker
    repository = Config.Sentry.repository
    dryRun = Config.Sentry.dryRun
    override = Config.Sentry.override
    artifactId = "sentry-core"
}

val VERSION_NAME = project.version.toString()
val DESCRIPTION = Config.Sentry.description
val SITE_URL = Config.Sentry.website
val GIT_URL = Config.Sentry.repository
val LICENSE = Config.Sentry.licence
val DEVELOPER_ID = "marandaneto"
val DEVELOPER_NAME = "Manoel Aranda Neto"
val DEVELOPER_EMAIL = "maranda@sentry.io"

gradle.taskGraph.whenReady {
    allTasks.find {
        it.path.contains("sentry-core:generatePomFileForMavenPublication")
    }?.doLast {
        println("delete file: " + file("build/publications/maven/pom-default.xml").delete())
        println("Overriding pom-file to make sure we can sync to maven central!")

        maven.pom {
            withGroovyBuilder {
                "project" {
                    "name"("${project.name}")
                    "artifactId"("sentry-core")
                    "packaging"("jar")
                    "description"("$DESCRIPTION")
                    "url"("$SITE_URL")
                    "version"("$VERSION_NAME")

                    "scm" {
                        "url"("$GIT_URL")
                        "connection"("$GIT_URL")
                        "developerConnection"("$GIT_URL")
                    }

                    "licenses" {
                        "license" {
                            "name"("$LICENSE")
                        }
                    }

                    "developers" {
                        "developer" {
                            "id"("$DEVELOPER_ID")
                            "name"("$DEVELOPER_NAME")
                            "email"("$DEVELOPER_EMAIL")
                        }
                    }
                }
            }
        }.writeTo("build/publications/maven/pom-default.xml")
    }
}
