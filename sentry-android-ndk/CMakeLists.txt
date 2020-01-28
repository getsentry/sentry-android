cmake_minimum_required(VERSION 3.4.1)
project("sentry-android")

# Adding sentry-native submodule subdirectory
add_subdirectory(${SENTRY_NATIVE_SRC})

# sentry-native submodule include path.
include_directories(${SENTRY_NATIVE_SRC}/include)

# https://android.googlesource.com/platform/ndk/+/master/docs/BuildSystemMaintainers.md#additional-required-arguments
set(CMAKE_SHARED_LINKER_FLAGS" ${CMAKE_SHARED_LINKER_FLAGS} -Wl,--build-id=sha1")

# sentry-native source code.
file(GLOB SENTRY_ANDROID_SOURCES ${CMAKE_SOURCE_DIR}/src/main/jni/*.c)

# Adding dynamic library and linking it with log and sentry-native.
add_library("sentry-android" SHARED ${SENTRY_ANDROID_SOURCES})
find_library(LOG_LIB log)
target_link_libraries("sentry-android" "sentry" ${LOG_LIB})
