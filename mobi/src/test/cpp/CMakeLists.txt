cmake_minimum_required(VERSION 3.22)

project(encoding_util_test LANGUAGES CXX)

include(CTest)

add_executable(encoding_util_test encoding_util_test.cpp)

target_compile_features(encoding_util_test PRIVATE cxx_std_17)
target_include_directories(encoding_util_test PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/../../main/cpp/util
    ${CMAKE_CURRENT_SOURCE_DIR}/../../main/cpp/utfcpp/source
)

add_test(NAME encoding_util_test COMMAND encoding_util_test)
