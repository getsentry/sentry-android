plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(Config.Android.compileSdkVersion)
    buildToolsVersion(Config.Android.buildToolsVersion)

    defaultConfig {
        applicationId = "io.sentry.sample"
        minSdkVersion(Config.Android.minSdkVersionNdk)
        targetSdkVersion(Config.Android.targetSdkVersion)
        versionCode = 2
        versionName = "1.1.0"

        externalNativeBuild {
            val sentryNativeSrc = if (File("${project.projectDir}/../sentry-android-ndk/sentry-native-local").exists()) {
                "sentry-native-local"
            } else {
                "sentry-native"
            }
            println("sentry-sample: $sentryNativeSrc")

            cmake {
                arguments.add(0, "-DANDROID_STL=c++_static")
                arguments.add(0, "-DSENTRY_NATIVE_SRC=$sentryNativeSrc")
            }
        }

        ndk {
            setAbiFilters(Config.Android.abiFilters)
            ndkVersion = Config.Android.ndkVersion
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // because of coreLibraryDesugaring
        multiDexEnabled = true
    }

    buildFeatures {
        // Determines whether to support View Binding.
        // Note that the viewBinding.enabled property is now deprecated.
        viewBinding = true
        // Determines whether to support injecting custom variables into the module's R class.
        resValues = false
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    externalNativeBuild {
        cmake {
            version = Config.Android.cmakeVersion
            setPath("CMakeLists.txt")
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders = mapOf(
                "sentryDebug" to true
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug") // to be able to run release mode
            isShrinkResources = true

            manifestPlaceholders = mapOf(
                "sentryDebug" to false
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // because of ChronoUnit API 25
        coreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":sentry-android"))

//    how to exclude androidx if release health feature is disabled
//    implementation(project(":sentry-android")) {
//        exclude(group = "androidx.lifecycle", module = "lifecycle-process")
//        exclude(group = "androidx.lifecycle", module = "lifecycle-common-java8")
//    }

    implementation(Config.Libs.appCompat)

    // debugging purpose
    implementation(Config.Libs.timber)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0-rc01")
    androidTestImplementation(Config.TestLibs.androidxRunner)
    androidTestImplementation(Config.TestLibs.androidxJunit)
    androidTestImplementation("androidx.test:rules:1.3.0-rc01")
    androidTestImplementation("androidx.test:core-ktx:1.3.0-rc01")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.2-rc01")
    debugImplementation(Config.Libs.leakCanary)
    androidTestImplementation(Config.TestLibs.awaitility) {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
        exclude(group = "org.hamcrest", module = "hamcrest-library")
        exclude(group = "org.hamcrest", module = "hamcrest")
    }

    // because of coreLibraryDesugaringEnabled
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.0.5")
}
