package io.sentry.android

import android.app.Application
import android.content.Context
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Config
import androidx.test.core.app.ApplicationProvider
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class,
    application = AndroidTest.ApplicationStub::class,
    sdk = [21])
abstract class AndroidTest {

    fun context(): Context {
        return ApplicationProvider.getApplicationContext()
    }

    internal class ApplicationStub : Application()
}
