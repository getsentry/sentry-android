import com.novoda.gradle.release.PublishExtension

plugins {
    id("com.android.library")
    kotlin("android")
    jacoco
    id(Config.Deploy.novodaBintray)
    id(Config.NativePlugins.nativeBundleExport)
    id(Config.QualityPlugins.gradleVersions)
}

android {
    compileSdkVersion(Config.Android.compileSdkVersion)
    buildToolsVersion(Config.Android.buildToolsVersion)

    val sentryNativeSrc = if (File("${project.projectDir}/sentry-native-local").exists()) {
        "sentry-native-local"
    } else {
        "sentry-native"
    }
    println("sentry-android-ndk: $sentryNativeSrc")

    defaultConfig {
        targetSdkVersion(Config.Android.targetSdkVersion)
        minSdkVersion(Config.Android.minSdkVersionNdk) // NDK requires a higher API level than core.

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionName = project.version.toString()
        versionCode = project.properties[Config.Sentry.buildVersionCodeProp].toString().toInt()

        externalNativeBuild {
            cmake {
                arguments.add(0, "-DANDROID_STL=c++_static")
                arguments.add(0, "-DSENTRY_NATIVE_SRC=$sentryNativeSrc")
            }
        }

        ndk {
            setAbiFilters(Config.Android.abiFilters)
            ndkVersion = Config.Android.ndkVersion
        }
    }

    buildFeatures {
        // Determines whether to generate a BuildConfig class.
        buildConfig = false
        // Determines whether to support injecting custom variables into the module's R class.
        resValues = false
    }

    externalNativeBuild {
        cmake {
            version = Config.Android.cmakeVersion
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

    nativeBundleExport {
        headerDir = "${project.projectDir}/$sentryNativeSrc/include"
    }
}

dependencies {
    api(project(":sentry-core"))
    api(project(":sentry-android-core"))

    compileOnly(Config.CompileOnly.jetbrainsAnnotations)
}

val initNative = tasks.register<Exec>("initNative") {
    logger.log(LogLevel.LIFECYCLE, "Initializing git submodules")
    commandLine("git", "submodule", "update", "--init", "--recursive")
}

tasks.named("preBuild") {
    dependsOn(initNative)
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
    setLicenceUrls(Config.Sentry.licenceUrl)
    issueTracker = Config.Sentry.issueTracker
    repository = Config.Sentry.repository
    sign = Config.Deploy.sign
    artifactId = project.name
    uploadName = "${project.group}:${project.name}"
    devId = Config.Sentry.userOrg
    devName = Config.Sentry.devName
    devEmail = Config.Sentry.devEmail
    scmConnection = Config.Sentry.scmConnection
    scmDevConnection = Config.Sentry.scmDevConnection
    scmUrl  = Config.Sentry.scmUrl
}
