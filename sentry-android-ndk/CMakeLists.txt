cmake_minimum_required(VERSION 3.4.1)
project("sentry-android")

# Adding sentry-native submodule subdirectory
add_subdirectory("sentry-native")

# sentry-native submodule include path.
include_directories("sentry-native/include")

# JNDCrash source code.
file(GLOB SENTRY_ANDROID_SOURCES ${CMAKE_SOURCE_DIR}/src/main/jni/*.c)

# Adding dynamic library and linking it with log and NDCrash.
add_library("sentry-android" SHARED ${SENTRY_ANDROID_SOURCES})
find_library(LOG_LIB log)
target_link_libraries("sentry-android" "sentry" ${LOG_LIB})
