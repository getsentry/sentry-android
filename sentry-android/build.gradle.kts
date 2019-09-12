plugins {
    id("com.android.library")
}

dependencies {
    api(project(":sentry-core"))
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(14)
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
}
