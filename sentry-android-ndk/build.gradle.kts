
plugins {
    id("com.android.library")
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

        minSdkVersion(21)
        externalNativeBuild {
            cmake {
                arguments.add(0, "-DANDROID_STL=c++_static")
                arguments.add(0, "-DCMAKE_VERBOSE_MAKEFILE:BOOL=ON")
            }
        }
        ndk {
            abiFilters("x86", "armeabi-v7a", "x86_64", "arm64-v8a")
        }

        missingDimensionStrategy(Config.Flavors.dimension, Config.Flavors.production)
    }

    externalNativeBuild {
        cmake {
            setPath("CMakeLists.txt")
        }
    }
}

dependencies {
    api(project(":sentry-core"))
    api(project(":sentry-android-core"))
}
