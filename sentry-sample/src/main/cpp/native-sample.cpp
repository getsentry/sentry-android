#include <jni.h>
#include <android/log.h>
//#include <sentry.h>

#define TAG "sentry-sample"

extern "C" {

JNIEXPORT void JNICALL Java_io_sentry_sample_NativeSample_crash(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "About to crash.");
    char *ptr = 0;
    *ptr += 1;
}

JNIEXPORT void JNICALL Java_io_sentry_sample_NativeSample_ndk_integration(JNIEnv *env, jclass cls) {
//    __android_log_print(ANDROID_LOG_WARN, TAG, "About to ndk_integration.");
//     sentry_value_t event = sentry_value_new_event();
//     sentry_value_set_by_key(event, "transaction", sentry_value_new_string("native yay"));

//      sentry_capture_event(event);
}

}
