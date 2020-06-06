package io.sentry.sample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import java.util.Timer
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.schedule
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
        onView(withId(R.id.capture_exception)).perform(click())
        onView(withId(R.id.breadcrumb)).perform(click())
        onView(withId(R.id.native_capture)).perform(click())

        val count = CountDownLatch(1)

        Timer(true).schedule(10000) {
            count.countDown()
        }

        // might be possible to replace with https://developer.android.com/training/testing/espresso/idling-resource
        // little waiting time for getting events processed
        count.await()
    }
}
