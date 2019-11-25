cmake_minimum_required(VERSION 3.4.1)
project("sentry-android-sample")

add_library(native-sample SHARED src/main/cpp/native-sample.cpp)
find_library(LOG_LIB log)
target_link_libraries(native-sample ${LOG_LIB})

set_target_properties(native-sample
        PROPERTIES
        LINK_FLAGS "-undefined dynamic_lookup"
    )
set(LOCAL_ALLOW_UNDEFINED_SYMBOLS true)

add_library(libsentry SHARED IMPORTED)

set(SENTRY_INCLUDE_DIR ${CMAKE_SOURCE_DIR}/assets/include)
target_include_directories(native-sample PRIVATE ${SENTRY_INCLUDE_DIR})
target_link_libraries(native-sample libsentry)
