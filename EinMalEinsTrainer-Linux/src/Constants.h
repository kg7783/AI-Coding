#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <SDL2/SDL_pixels.h>
#include <SDL2/SDL_rect.h>

namespace Constants {
    constexpr int WINDOW_WIDTH = 800;
    constexpr int WINDOW_HEIGHT = 600;
    constexpr const char* WINDOW_TITLE = "EinMalEins Trainer";

    constexpr int FIELD_WIDTH = 100;
    constexpr int FIELD_HEIGHT = 70;
    constexpr int FIELD_SPACING = 15;
    constexpr int ROW_SPACING = 100;
    constexpr int START_Y = 130;

    constexpr SDL_Color BACKGROUND_COLOR = {135, 206, 235, 255};
    constexpr SDL_Color FIELD_BG_COLOR = {255, 255, 255, 255};
    constexpr SDL_Color TEXT_COLOR = {0, 0, 0, 255};
    constexpr SDL_Color OPERATOR_COLOR = {50, 50, 50, 255};
    constexpr SDL_Color CORRECT_COLOR = {34, 139, 34, 255};
    constexpr SDL_Color WRONG_COLOR = {220, 20, 60, 255};
    constexpr SDL_Color INPUT_BG_COLOR = {230, 230, 250, 255};
    constexpr SDL_Color INPUT_BORDER_COLOR = {100, 100, 200, 255};
    constexpr SDL_Color SELECTED_COLOR = {100, 200, 100, 255};
    constexpr SDL_Color UNSELECTED_COLOR = {200, 200, 200, 255};
    constexpr SDL_Color BUTTON_COLOR = {70, 130, 180, 255};

    constexpr int ANIMATION_DURATION_MS = 300;
    constexpr int INPUT_DELAY_MS = 2000;
    constexpr int FONT_SIZE = 24;
    constexpr int FONT_SIZE_LARGE = 40;
    constexpr int FONT_SIZE_XLARGE = 52;
    constexpr int FONT_SIZE_SMALL = 18;

    constexpr int DIVISORS[] = {2, 5, 10};
    constexpr int NUM_DIVISORS = 3;
}

#endif
