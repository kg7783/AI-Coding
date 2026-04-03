cmake_minimum_required(VERSION 3.10)
project(EinMalEinsTrainer VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_BUILD_TYPE Debug)

find_package(PkgConfig)
pkg_check_modules(SDL2 REQUIRED sdl2 SDL2_ttf)

include_directories(${SDL2_INCLUDE_DIRS})

add_executable(${PROJECT_NAME} 
    src/main.cpp
    src/Constants.h
    src/Trainer.cpp
    src/Trainer.h
    src/ConfigManager.cpp
    src/ConfigManager.h
    src/GUI.cpp
    src/GUI.h
)

target_link_libraries(${PROJECT_NAME} ${SDL2_LIBRARIES})
