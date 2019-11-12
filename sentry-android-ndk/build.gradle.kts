import com.novoda.gradle.release.PublishExtension

plugins {
    id("com.android.library")
    kotlin("android")
    jacoco
    maven
}

apply(plugin = Config.Deploy.bintrayPlugin)

android {
    compileSdkVersion(Config.Android.compileSdkVersion)
    buildToolsVersion(Config.Android.buildToolsVersion)

    defaultConfig {
        targetSdkVersion(Config.Android.targetSdkVersion)
        minSdkVersion(Config.Android.minSdkVersionNdk) // NDK requires a higher API level than core.

        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = true
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionName = project.version.toString()
        versionCode = Config.Sentry.buildVersionCode

        externalNativeBuild {
            val sentryNativeSrc = if (File("${project.projectDir}/sentry-native-local").exists()) {
                "sentry-native-local"
            } else {
                "sentry-native"
            }
            cmake {
                arguments.add(0, "-DANDROID_STL=c++_static")
                arguments.add(0, "-DCMAKE_VERBOSE_MAKEFILE:BOOL=ON")
                arguments.add(0, "-DSENTRY_NATIVE_SRC=$sentryNativeSrc")
            }
        }

        ndk {
            val platform = System.getenv("ABI")
            if (platform == null || platform.toLowerCase() == "all") {
                abiFilters("x86", "armeabi-v7a", "x86_64", "arm64-v8a")
            } else {
                abiFilters(platform)
            }
        }

        // replace with https://issuetracker.google.com/issues/72050365 once released.
        libraryVariants.all {
            generateBuildConfigProvider?.configure {
                enabled = false
            }
        }
    }

    externalNativeBuild {
        cmake {
            setPath("CMakeLists.txt")
        }
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

    ndkVersion = "20.1.5948944"
}

dependencies {
    api(project(":sentry-core"))
    api(project(":sentry-android-core"))

    compileOnly(Config.CompileOnly.annotations)
}

val initNative = tasks.register<Exec>("initNative") {
    logger.log(LogLevel.LIFECYCLE, "Initializing git submodules")
    commandLine("git", "submodule", "update", "--init", "--recursive")
}

tasks.named("preBuild") {
    dependsOn(initNative)
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
    artifactId = "sentry-android-ndk"
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
        it.path.contains("sentry-android-ndk:generatePomFileForReleasePublication")
    }?.doLast {
        println("delete file: " + file("build/publications/release/pom-default.xml").delete())
        println("Overriding pom-file to make sure we can sync to maven central!")

        maven.pom {
            withGroovyBuilder {
                "project" {
                    "name"("${project.name}")
                    "artifactId"("sentry-android-ndk")
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
