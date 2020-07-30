package io.sentry.core.protocol

import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class AppTest {
    @Test
    fun `cloning app wont have the same references`() {
        val app = App()
        app.appBuild = "app build"
        app.appIdentifier = "app identifer"
        app.appName = "app name"
        app.appStartTime = Date()
        app.appVersion = "app version"
        app.buildType = "build type"
        app.deviceAppHash = "device app hash"
        val unknown = mapOf(Pair("unknown", "unknown"))
        app.acceptUnknownProperties(unknown)

        val clone = app.clone()

        assertNotNull(clone)
        assertNotSame(app, clone)
        assertNotSame(app.appStartTime, clone.appStartTime)

        assertNotSame(app.unknown, clone.unknown)
    }
}
