cmake_minimum_required(VERSION 3.4.1)
project("sentry-android-sample")

add_library(libsentry SHARED IMPORTED)
add_library(libsentry-android SHARED IMPORTED)

set(SENTRY_NATIVE_DIR
    ${CMAKE_SOURCE_DIR}/native)

set(SENTRY_INCLUDE_DIR ${SENTRY_NATIVE_DIR}/include)

set_target_properties(libsentry PROPERTIES IMPORTED_LOCATION
                      ${SENTRY_NATIVE_DIR}/${ANDROID_ABI}/libsentry.so)

set_target_properties(libsentry-android PROPERTIES IMPORTED_LOCATION
                      ${SENTRY_NATIVE_DIR}/${ANDROID_ABI}/libsentry-android.so)

add_library(native-sample SHARED src/main/cpp/native-sample.cpp)
find_library(LOG_LIB log)

target_include_directories(native-sample PRIVATE ${SENTRY_INCLUDE_DIR})

target_link_libraries(native-sample ${LOG_LIB} libsentry libsentry-android)
