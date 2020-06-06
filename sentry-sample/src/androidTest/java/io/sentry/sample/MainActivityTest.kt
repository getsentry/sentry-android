package io.sentry.sample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import java.util.Timer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule
import org.awaitility.kotlin.await
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val rule = activityScenarioRule<MainActivity>()

    @Test
    fun mainActivityTest() {
        onView(withId(R.id.send_message)).perform(click())
        onView(withId(R.id.native_capture)).perform(click())
        onView(withId(R.id.breadcrumb)).perform(click())

        val wait5s = AtomicBoolean(false)

        // Didn't find class "java.time.temporal.ChronoUnit" throws API 25
        Timer(true).schedule(5000) {
            wait5s.set(true)
        }

        // might be possible to replace with https://developer.android.com/training/testing/espresso/idling-resource
        // little waiting time for getting events processed
        await.untilTrue(wait5s)
    }
}
