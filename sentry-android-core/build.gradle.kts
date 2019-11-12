import org.jetbrains.kotlin.config.KotlinCompilerVersion
import com.novoda.gradle.release.PublishExtension

plugins {
    id("com.android.library")
    kotlin("android")
    jacoco
    id("net.ltgt.errorprone")
    maven
}

apply(plugin = Config.Deploy.bintrayPlugin)

android {
    compileSdkVersion(Config.Android.compileSdkVersion)
    buildToolsVersion(Config.Android.buildToolsVersion)

    defaultConfig {
        targetSdkVersion(Config.Android.targetSdkVersion)
        minSdkVersion(Config.Android.minSdkVersion)

        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = true
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionName = project.version.toString()
        versionCode = Config.Sentry.buildVersionCode

        buildConfigField("String", "SENTRY_CLIENT_NAME", "\"${Config.Sentry.SENTRY_CLIENT_NAME}\"")
    }

    buildTypes {
        getByName("debug")
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    testOptions {
        animationsDisabled = true
        unitTests.apply {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
            all(KotlinClosure1<Any, Test>({
                (this as Test).also { testTask ->
                    testTask.extensions
                        .getByType(JacocoTaskExtension::class.java)
                        .isIncludeNoLocationClasses = true
                }
            }, this))
        }
    }

    lintOptions {
        isWarningsAsErrors = true
        isCheckDependencies = true

        // We run a full lint analysis as build part in CI, so skip vital checks for assemble tasks.
        isCheckReleaseBuilds = false
    }
}

dependencies {
    api(project(":sentry-core"))

    // libs
    implementation(Config.Libs.gson)

    compileOnly(Config.CompileOnly.noopen)
    errorprone(Config.CompileOnly.noopenProne)
    errorprone(Config.CompileOnly.errorprone)
    errorproneJavac(Config.CompileOnly.errorProneJavac)
    compileOnly(Config.CompileOnly.annotations)

    // tests
    testImplementation(kotlin(Config.kotlinStdLib, KotlinCompilerVersion.VERSION))
    testImplementation(Config.TestLibs.robolectric)
    testImplementation(Config.TestLibs.kotlinTestJunit)
    testImplementation(Config.TestLibs.androidxCore)
    testImplementation(Config.TestLibs.androidxRunner)
    testImplementation(Config.TestLibs.androidxJunit)
    testImplementation(Config.TestLibs.mockitoKotlin)
}

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
    artifactId = "sentry-android-core"
}

//TODO: move this block to parent gradle class, DRY
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
        it.path.contains("sentry-android-core:generatePomFileForReleasePublication")
    }?.doLast {
        println("delete file: " + file("build/publications/release/pom-default.xml").delete())
        println("Overriding pom-file to make sure we can sync to maven central!")

        maven.pom {
            withGroovyBuilder {
                "project" {
                    "name"("${project.name}")
                    "artifactId"("sentry-android-core")
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
