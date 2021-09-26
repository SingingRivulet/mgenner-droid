# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("midilib")

set(CMAKE_CXX_FLAGS "-std=c++2a -O3")

include_directories(
        ./)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        midiPlayerService/midiPlayerService.cpp

        #mgenner
        mgenner/Options.cpp
        mgenner/Binasc.cpp
        mgenner/MidiMessage.cpp
        mgenner/MidiEvent.cpp
        mgenner/MidiEventList.cpp
        mgenner/MidiFile.cpp
        mgenner/midiLoader.cpp
        mgenner/midiMap.cpp
        mgenner/midisplit.cpp
        mgenner/player.cpp
        mgenner/synth.cpp
        mgenner/synthOutput.cpp
        mgenner/editTable.cpp
        mgenner/hbb.cpp
        mgenner/mgenner.cpp

        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(
        log-lib
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${android-lib}
        ${log-lib}
        android)