#include <sentry.h>
#include <jni.h>
#include <malloc.h>
#include <android/log.h>

void print(sentry_value_t event) {
    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "EVENTTTTTTTTTTT: %s",sentry_value_to_json(event));
}

JNIEXPORT void JNICALL Java_io_sentry_android_ndk_SentryNdk_example(JNIEnv *env, jclass clazz) {
    sentry_options_t *options = sentry_options_new();

    sentry_options_set_transport(options, print, NULL);

    sentry_options_set_environment(options, "Production");
    sentry_options_set_release(options, "5fd7a6cd");
    sentry_options_set_debug(options, 1);
    sentry_options_set_dsn(options, "http://dfbecfd398754c73b6e8104538e89124@sentry.io/1322857");

    sentry_init(options);

    sentry_value_t event = sentry_value_new_event();
    sentry_value_set_by_key(event, "message",
                            sentry_value_new_string("Hello World!"));

    sentry_capture_event(event);

    sentry_shutdown();

}

