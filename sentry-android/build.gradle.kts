import com.novoda.gradle.release.PublishExtension

plugins {
    id("com.android.library")
    kotlin("android")
    maven
    id(Config.Deploy.bintrayPlugin)
}

android {
    compileSdkVersion(Config.Android.compileSdkVersion)
    buildToolsVersion(Config.Android.buildToolsVersion)

    defaultConfig {
        targetSdkVersion(Config.Android.targetSdkVersion)
        minSdkVersion(Config.Android.minSdkVersionNdk)

        versionName = project.version.toString()
        versionCode = Config.Sentry.buildVersionCode
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // replace with https://issuetracker.google.com/issues/72050365 once released.
    libraryVariants.all {
        generateBuildConfigProvider?.configure {
            enabled = false
        }
    }
}

dependencies {
    api(project(":sentry-android-core"))
    api(project(":sentry-android-ndk"))
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
    artifactId = "sentry-android"
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
        it.path.contains("sentry-android:generatePomFileForReleasePublication")
    }?.doLast {
        println("delete file: " + file("build/publications/release/pom-default.xml").delete())
        println("Overriding pom-file to make sure we can sync to maven central!")

        maven.pom {
            withGroovyBuilder {
                "project" {
                    "name"("${project.name}")
                    "artifactId"("sentry-android")
                    "packaging"("aar")
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
        }.writeTo("build/publications/release/pom-default.xml")
    }
}
