import com.novoda.gradle.release.PublishExtension
import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    kotlin("android")
    jacoco
    id(Config.QualityPlugins.errorProne)
    id(Config.Deploy.novodaBintray)
    id(Config.QualityPlugins.gradleVersions)
}

android {
    compileSdkVersion(Config.Android.compileSdkVersion)
    buildToolsVersion(Config.Android.buildToolsVersion)

    defaultConfig {
        targetSdkVersion(Config.Android.targetSdkVersion)
        minSdkVersion(Config.Android.minSdkVersion)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        versionName = project.version.toString()
        versionCode = project.properties[Config.Sentry.buildVersionCodeProp].toString().toInt()
    }

    buildTypes {
        getByName("debug")
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    buildFeatures {
        // Determines whether to generate a BuildConfig class.
        buildConfig = false
        // Determines whether to support injecting custom variables into the module's R class.
        resValues = false
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
//    api(project(":sentry-android-core"))

    api(Config.Libs.timber)

    errorprone(Config.CompileOnly.errorprone)
    errorproneJavac(Config.CompileOnly.errorProneJavac8)

    implementation(kotlin(Config.kotlinStdLib, KotlinCompilerVersion.VERSION))

    // tests
    testImplementation(Config.TestLibs.kotlinTestJunit)
    testImplementation(Config.TestLibs.androidxJunit)
    testImplementation(Config.TestLibs.mockitoKotlin)
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
repositories {
    mavenCentral()
}