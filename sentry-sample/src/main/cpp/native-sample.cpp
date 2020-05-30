#include <jni.h>
#include <android/log.h>
#include <sentry.h>

#define TAG "sentry-sample"

extern "C" {

JNIEXPORT void JNICALL Java_io_sentry_sample_NativeSample_crash(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "About to crash.");
    char *ptr = 0;
    *ptr += 1;
}

JNIEXPORT void JNICALL Java_io_sentry_sample_NativeSample_message(JNIEnv *env, jclass cls) {
    __android_log_print(ANDROID_LOG_WARN, TAG, "Sending message.");

    jclass clazz = env->FindClass("io/sentry/sample/NativeSample");
    jmethodID method = env->GetStaticMethodID(clazz, "test",
                                              "()Ljava/lang/String;");
    jstring result = (jstring) env->CallStaticObjectMethod(clazz, method);
    // if I do a NPE check, it bails out and it works
//    if (result == NULL) {
//        return;
//    }

// lets keep running to segfault as result is NULL

    const char *nativeString = env->GetStringUTFChars(result, 0);

    sentry_value_t event = sentry_value_new_message_event(
            /*   level */ SENTRY_LEVEL_INFO,
            /*  logger */ "custom",
            /* message */ nativeString
    );
    sentry_capture_event(event);
}

}
