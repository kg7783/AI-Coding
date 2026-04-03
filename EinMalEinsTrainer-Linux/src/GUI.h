#ifndef GUI_H
#define GUI_H

#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <string>
#include <functional>

#include "Trainer.h"
#include "Constants.h"

enum class AnimationState {
    NONE,
    GROWING,
    SHRINKING
};

enum class Screen {
    SETUP,
    TRAINING
};

struct InputField {
    SDL_Rect rect;
    std::string text;
    bool isActive;
    Uint32 lastInputTime;
    bool pendingCheck;
    bool isCorrect;
    AnimationState animState;
    Uint32 animStartTime;
};

class GUI {
public:
    GUI();
    ~GUI();

    bool init();
    void run();

private:
    SDL_Window* window;
    SDL_Renderer* renderer;
    TTF_Font* font;
    TTF_Font* largeFont;
    TTF_Font* xlargeFont;
    TTF_Font* smallFont;

    Trainer trainer;
    InputField multAnswerField;
    InputField divAnswerField;

    int selectedField;

    Screen currentScreen;

    SDL_Rect baseNumberButtons[10];
    SDL_Rect multiplierButtons[10][10];
    SDL_Rect startButton;
    SDL_Rect backButton;
    SDL_Rect setupButton;
    int selectedBaseForMultipliers;

    void renderSetup();
    void renderTraining();
    void handleSetupClick(int mx, int my);

    void render();
    void renderRow(int row, int y, const MathProblem& problem, InputField& answerField);
    void renderField(int x, int y, const std::string& text, bool isInput, bool isActive, const SDL_Color& bgColor, const SDL_Color& textColor, float scale = 1.0f, TTF_Font* usedFont = nullptr);
    void renderText(const std::string& text, int x, int y, const SDL_Color& color, TTF_Font* usedFont = nullptr);
    void renderTextCentered(const std::string& text, int y, const SDL_Color& color, TTF_Font* usedFont = nullptr);

    void handleEvent(const SDL_Event& event);
    void handleKeyDown(const SDL_KeyboardEvent& key);
    void handleMouseDown(int mx, int my);
    void handleTextInput(const char* text);

    void checkAnswers();
    void updateAnimations();
    void resetGame();

    int getFieldX(int col) const;
    int getRowY(int row) const;
};

#endif
