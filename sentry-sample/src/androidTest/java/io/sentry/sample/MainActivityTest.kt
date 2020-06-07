package io.sentry.sample

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import io.sentry.core.protocol.SentryId
import java.util.Timer
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.schedule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import okhttp3.ResponseBody
import org.junit.Rule
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://sentry.io/")
//            .client(OkHttpClient.Builder().addInterceptor { chain ->
//                // token from https://sentry.io/settings/account/api/auth-tokens/
//                val request = chain.request().newBuilder().addHeader("Authorization", "Bearer ${TOKEN}}").build()
//                chain.proceed(request)
//            }.build())
//            .build()
//        val service = retrofit.create(SentryService::class.java)

//        val count = CountDownLatch(4)

        rule.scenario.onActivity {
            val ids = it.ids
            assertEquals(4, ids.size)
//            val apiCallback = ApiCallback(count)
            ids.forEach { id ->
                assertNotEquals(SentryId.EMPTY_ID, id)

                // TODO: try 5 times with interval of 1 sec? event might not be processed yet
//                service.getEvent(id).enqueue(apiCallback)
            }
        }

        val count = CountDownLatch(1)

        Timer(true).schedule(5000) {
            count.countDown()
        }

        // might be possible to replace with https://developer.android.com/training/testing/espresso/idling-resource
        // little waiting time for getting events processed
//        count.await(1, TimeUnit.MINUTES)
        count.await()
    }

    internal class ApiCallback(private val count: CountDownLatch) : Callback<ResponseBody> {

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("Sentry", "error: ${t.message}", t)
            throw t
        }

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            // TODO: assert its content? right now ony checking if the event exist
            if (!response.isSuccessful) {
                Log.i("Sentry", response.message())
                Log.e("Sentry", "error: ${response.errorBody()?.string()}")
                Log.i("Sentry", "http code: ${response.code()}")
            } else {
                Log.i("Sentry", "success: ${response.body()?.string()}")
            }
            assertTrue(response.isSuccessful)
            count.countDown()
        }
    }
}
