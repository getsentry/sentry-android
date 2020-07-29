plugins {
    java
    id(Config.QualityPlugins.gradleVersions)
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":sentry-core"))
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveFileName.set("${project.name}-fat.jar")
    manifest {
        attributes["Main-Class"] = "io.sentry.samples.console.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
