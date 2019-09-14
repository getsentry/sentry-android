plugins {
    id("com.android.library")
    jacoco
}

dependencies {
    api(project(":sentry-core"))
    testImplementation("org.robolectric:robolectric:4.3")
    testImplementation(kotlin("stdlib"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.3.50")
    testImplementation("junit:junit:4.12")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(14)
        javaCompileOptions {
            annotationProcessorOptions {
                includeCompileClasspath = true
            }
        }
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
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
}
