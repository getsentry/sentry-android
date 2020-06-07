package io.sentry.sample

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SentryService {
    @GET("api/0/projects/sentry-test/android/events/{id}/")
    fun getEvent(@Path("id") id: String): Call<ResponseBody>
}
