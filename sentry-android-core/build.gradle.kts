import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    kotlin("android")
    jacoco
}

android {
    compileSdkVersion(Config.Android.compileSdkVersion)
    buildToolsVersion(Config.Android.buildToolsVersion)

    defaultConfig {
        targetSdkVersion(Config.Android.targetSdkVersion)
        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = true
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        defaultPublishConfig = "${Config.Flavors.production}Release"
    }

    buildTypes {
        getByName("debug")
        getByName("release") {
//            isMinifyEnabled = true // Do we want to enable it for our lib?
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    flavorDimensions(Config.Flavors.dimension)

    productFlavors {
        create(Config.Flavors.staging) {
            minSdkVersion(Config.Android.minSdkVersionDebug)
        }
        create(Config.Flavors.production) {
            minSdkVersion(Config.Android.minSdkVersion)
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
    }
}

dependencies {
    api(project(":sentry-core"))

    // libs
    implementation(Config.Libs.gson)

    // tests
    testImplementation(kotlin(Config.kotlinStdLib, KotlinCompilerVersion.VERSION))
    testImplementation(Config.TestLibs.robolectric)
    testImplementation(Config.TestLibs.kotlinTestJunit)
    testImplementation(Config.TestLibs.androidxCore)
    testImplementation(Config.TestLibs.androidxRunner)
    testImplementation(Config.TestLibs.androidxJunit)
    testImplementation(Config.TestLibs.mockitoKotlin)
}
