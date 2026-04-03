#include "GUI.h"
#include <iostream>
#include <sstream>
#include <iomanip>

GUI::GUI() 
    : window(nullptr)
    , renderer(nullptr)
    , font(nullptr)
    , largeFont(nullptr)
    , xlargeFont(nullptr)
    , smallFont(nullptr)
    , selectedField(-1)
    , currentScreen(Screen::TRAINING)
    , selectedBaseForMultipliers(0)
{
    multAnswerField = {0, 0, 0, 0, "", false, 0, false, false, AnimationState::NONE, 0};
    divAnswerField = {0, 0, 0, 0, "", false, 0, false, false, AnimationState::NONE, 0};
}

GUI::~GUI() {
    if (smallFont) TTF_CloseFont(smallFont);
    if (xlargeFont) TTF_CloseFont(xlargeFont);
    if (largeFont) TTF_CloseFont(largeFont);
    if (font) TTF_CloseFont(font);
    if (renderer) SDL_DestroyRenderer(renderer);
    if (window) SDL_DestroyWindow(window);
    TTF_Quit();
    SDL_Quit();
}

bool GUI::init() {
    if (SDL_Init(SDL_INIT_VIDEO) < 0) {
        std::cerr << "SDL init failed: " << SDL_GetError() << std::endl;
        return false;
    }

    if (TTF_Init() < 0) {
        std::cerr << "TTF init failed: " << TTF_GetError() << std::endl;
        return false;
    }

    window = SDL_CreateWindow(
        Constants::WINDOW_TITLE,
        SDL_WINDOWPOS_CENTERED,
        SDL_WINDOWPOS_CENTERED,
        Constants::WINDOW_WIDTH,
        Constants::WINDOW_HEIGHT,
        SDL_WINDOW_SHOWN
    );

    if (!window) {
        std::cerr << "Window creation failed: " << SDL_GetError() << std::endl;
        return false;
    }

    SDL_ShowWindow(window);

    renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED | SDL_RENDERER_PRESENTVSYNC);
    if (!renderer) {
        std::cerr << "Renderer creation failed: " << SDL_GetError() << std::endl;
        return false;
    }

    font = TTF_OpenFont("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", Constants::FONT_SIZE);
    largeFont = TTF_OpenFont("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", Constants::FONT_SIZE_LARGE);
    xlargeFont = TTF_OpenFont("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", Constants::FONT_SIZE_XLARGE);
    smallFont = TTF_OpenFont("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", Constants::FONT_SIZE_SMALL);

    if (!font || !largeFont || !xlargeFont || !smallFont) {
        std::cerr << "Font loading failed, trying fallback" << std::endl;
        font = TTF_OpenFont("/usr/share/fonts/TTF/DejaVuSans.ttf", Constants::FONT_SIZE);
        largeFont = TTF_OpenFont("/usr/share/fonts/TTF/DejaVuSans.ttf", Constants::FONT_SIZE_LARGE);
        xlargeFont = TTF_OpenFont("/usr/share/fonts/TTF/DejaVuSans.ttf", Constants::FONT_SIZE_XLARGE);
        smallFont = TTF_OpenFont("/usr/share/fonts/TTF/DejaVuSans.ttf", Constants::FONT_SIZE_SMALL);
        
        if (!font) {
            font = TTF_OpenFont(nullptr, Constants::FONT_SIZE);
            largeFont = TTF_OpenFont(nullptr, Constants::FONT_SIZE_LARGE);
            xlargeFont = TTF_OpenFont(nullptr, Constants::FONT_SIZE_XLARGE);
            smallFont = TTF_OpenFont(nullptr, Constants::FONT_SIZE_SMALL);
        }
        
        if (!font) {
            std::cerr << "Could not load any font" << std::endl;
            return false;
        }
    }

    int startX = (Constants::WINDOW_WIDTH - (5 * Constants::FIELD_WIDTH + 4 * Constants::FIELD_SPACING + 60)) / 2;
    multAnswerField.rect = {
        startX + 4 * (Constants::FIELD_WIDTH + Constants::FIELD_SPACING) + 60,
        Constants::START_Y,
        Constants::FIELD_WIDTH,
        Constants::FIELD_HEIGHT
    };
    divAnswerField.rect = {
        startX + 4 * (Constants::FIELD_WIDTH + Constants::FIELD_SPACING) + 60,
        Constants::START_Y + Constants::FIELD_HEIGHT + Constants::ROW_SPACING,
        Constants::FIELD_WIDTH,
        Constants::FIELD_HEIGHT
    };

    int buttonSize = 50;
    int buttonSpacing = 10;
    int baseStartX = 50;
    int baseStartY = 100;

    for (int i = 0; i < 10; i++) {
        baseNumberButtons[i] = {
            baseStartX + i * (buttonSize + buttonSpacing),
            baseStartY,
            buttonSize,
            buttonSize
        };
    }

    int multStartX = 50;
    int multStartY = 180;

    for (int base = 0; base < 10; base++) {
        for (int mult = 0; mult < 10; mult++) {
            multiplierButtons[base][mult] = {
                multStartX + mult * (buttonSize + 5),
                multStartY + base * (buttonSize + 5),
                buttonSize,
                buttonSize
            };
        }
    }

    startButton = {Constants::WINDOW_WIDTH - 200, Constants::WINDOW_HEIGHT - 80, 150, 50};
    backButton = {50, Constants::WINDOW_HEIGHT - 80, 150, 50};
    setupButton = {Constants::WINDOW_WIDTH - 200, 60, 150, 40};

    SDL_StartTextInput();

    return true;
}

int GUI::getFieldX(int col) const {
    int startX = (Constants::WINDOW_WIDTH - (5 * Constants::FIELD_WIDTH + 4 * Constants::FIELD_SPACING + 60)) / 2;
    return startX + col * (Constants::FIELD_WIDTH + Constants::FIELD_SPACING);
}

int GUI::getRowY(int row) const {
    return Constants::START_Y + row * (Constants::FIELD_HEIGHT + Constants::ROW_SPACING);
}

void GUI::run() {
    bool running = true;
    SDL_Event event;

    while (running) {
        while (SDL_PollEvent(&event)) {
            handleEvent(event);
            if (event.type == SDL_QUIT) {
                running = false;
            }
        }

        if (currentScreen == Screen::TRAINING) {
            checkAnswers();
            updateAnimations();
        }
        render();

        SDL_Delay(16);
    }
}

void GUI::handleEvent(const SDL_Event& event) {
    switch (event.type) {
        case SDL_KEYDOWN:
            handleKeyDown(event.key);
            break;
        case SDL_MOUSEBUTTONDOWN:
            if (currentScreen == Screen::SETUP) {
                handleSetupClick(event.button.x, event.button.y);
            } else {
                handleMouseDown(event.button.x, event.button.y);
            }
            break;
        case SDL_TEXTINPUT:
            handleTextInput(event.text.text);
            break;
    }
}

void GUI::handleKeyDown(const SDL_KeyboardEvent& key) {
    if (currentScreen == Screen::SETUP) {
        if (key.keysym.sym == SDLK_ESCAPE) {
            currentScreen = Screen::TRAINING;
            trainer.generateNewProblems();
            return;
        }
        return;
    }

    if (key.keysym.sym == SDLK_TAB) {
        selectedField = (selectedField + 1) % 2;
        multAnswerField.isActive = (selectedField == 0);
        divAnswerField.isActive = (selectedField == 1);
        return;
    }

    if (key.keysym.sym == SDLK_BACKSPACE) {
        if (selectedField == 0 && !multAnswerField.text.empty()) {
            multAnswerField.text.pop_back();
            trainer.setMultiplicationAnswer(multAnswerField.text);
        } else if (selectedField == 1 && !divAnswerField.text.empty()) {
            divAnswerField.text.pop_back();
            trainer.setDivisionAnswer(divAnswerField.text);
        }
        return;
    }

    if (key.keysym.sym == SDLK_RETURN) {
        if (selectedField == 0) {
            multAnswerField.lastInputTime = SDL_GetTicks();
            multAnswerField.pendingCheck = true;
        } else if (selectedField == 1) {
            divAnswerField.lastInputTime = SDL_GetTicks();
            divAnswerField.pendingCheck = true;
        }
        return;
    }

    if (key.keysym.sym >= SDLK_0 && key.keysym.sym <= SDLK_9) {
        char c = '0' + (key.keysym.sym - SDLK_0);
        handleTextInput(&c);
    }

    if (key.keysym.sym >= SDLK_KP_0 && key.keysym.sym <= SDLK_KP_9) {
        char c = '0' + (key.keysym.sym - SDLK_KP_0);
        handleTextInput(&c);
    }
}

void GUI::handleSetupClick(int mx, int my) {
    SDL_Point point = {mx, my};

    for (int i = 0; i < 10; i++) {
        if (SDL_PointInRect(&point, &baseNumberButtons[i])) {
            int num = i + 1;
            auto& config = trainer.getConfig();
            bool hasMultipliers = !config.multipliers[num].empty();
            selectedBaseForMultipliers = num;
            return;
        }
    }

    if (selectedBaseForMultipliers > 0) {
        int multStartX = 50;
        int multStartY = 225;
        int buttonSize = 50;
        int buttonSpacing = 10;

        for (int j = 0; j < 10; j++) {
            SDL_Rect btn = {multStartX + j * (buttonSize + buttonSpacing), multStartY, buttonSize, buttonSize};
            if (SDL_PointInRect(&point, &btn)) {
                int mult = j + 1;
                auto& config = trainer.getConfig();
                if (config.multipliers[selectedBaseForMultipliers].count(mult)) {
                    trainer.setMultiplierSelected(selectedBaseForMultipliers, mult, false);
                } else {
                    trainer.setMultiplierSelected(selectedBaseForMultipliers, mult, true);
                }
                return;
            }
        }
    }

    if (SDL_PointInRect(&point, &startButton)) {
        if (!trainer.getConfig().baseNumbers.empty()) {
            currentScreen = Screen::TRAINING;
            trainer.generateNewProblems();
        }
        return;
    }
}

void GUI::handleMouseDown(int mx, int my) {
    SDL_Point point = {mx, my};

    if (SDL_PointInRect(&point, &setupButton)) {
        currentScreen = Screen::SETUP;
        trainer.generateNewProblems();
        
        auto& config = trainer.getConfig();
        selectedBaseForMultipliers = 0;
        for (int i = 1; i <= 10; i++) {
            if (!config.multipliers[i].empty()) {
                selectedBaseForMultipliers = i;
                break;
            }
        }
        return;
    }

    multAnswerField.isActive = SDL_PointInRect(&point, &multAnswerField.rect);
    divAnswerField.isActive = SDL_PointInRect(&point, &divAnswerField.rect);
    
    if (multAnswerField.isActive) selectedField = 0;
    else if (divAnswerField.isActive) selectedField = 1;
}

void GUI::handleTextInput(const char* text) {
    if (selectedField == 0 && multAnswerField.text.length() < 10) {
        multAnswerField.text += text;
        multAnswerField.lastInputTime = SDL_GetTicks();
        multAnswerField.pendingCheck = true;
        trainer.setMultiplicationAnswer(multAnswerField.text);
    } else if (selectedField == 1 && divAnswerField.text.length() < 10) {
        divAnswerField.text += text;
        divAnswerField.lastInputTime = SDL_GetTicks();
        divAnswerField.pendingCheck = true;
        trainer.setDivisionAnswer(divAnswerField.text);
    }
}

void GUI::checkAnswers() {
    Uint32 currentTime = SDL_GetTicks();

    if (multAnswerField.pendingCheck && 
        currentTime - multAnswerField.lastInputTime > Constants::INPUT_DELAY_MS) {
        
        multAnswerField.pendingCheck = false;
        multAnswerField.isCorrect = trainer.isMultiplicationCorrect();
        
        if (multAnswerField.animState == AnimationState::NONE) {
            multAnswerField.animState = multAnswerField.isCorrect ? 
                AnimationState::GROWING : AnimationState::SHRINKING;
            multAnswerField.animStartTime = currentTime;
        }

        if (!multAnswerField.isCorrect && !multAnswerField.text.empty()) {
            multAnswerField.text.clear();
        }
        
        if (multAnswerField.isCorrect) {
            multAnswerField.text.clear();
            trainer.generateMultiplicationProblem();
        }
    }

    if (divAnswerField.pendingCheck && 
        currentTime - divAnswerField.lastInputTime > Constants::INPUT_DELAY_MS) {
        
        divAnswerField.pendingCheck = false;
        divAnswerField.isCorrect = trainer.isDivisionCorrect();
        
        if (divAnswerField.animState == AnimationState::NONE) {
            divAnswerField.animState = divAnswerField.isCorrect ? 
                AnimationState::GROWING : AnimationState::SHRINKING;
            divAnswerField.animStartTime = currentTime;
        }

        if (!divAnswerField.isCorrect && !divAnswerField.text.empty()) {
            divAnswerField.text.clear();
        }
        
        if (divAnswerField.isCorrect) {
            divAnswerField.text.clear();
            trainer.generateDivisionProblem();
        }
    }
}

void GUI::updateAnimations() {
    Uint32 currentTime = SDL_GetTicks();

    if (multAnswerField.animState != AnimationState::NONE) {
        Uint32 elapsed = currentTime - multAnswerField.animStartTime;
        if (elapsed >= Constants::ANIMATION_DURATION_MS * 2) {
            multAnswerField.animState = AnimationState::NONE;
        }
    }

    if (divAnswerField.animState != AnimationState::NONE) {
        Uint32 elapsed = currentTime - divAnswerField.animStartTime;
        if (elapsed >= Constants::ANIMATION_DURATION_MS * 2) {
            divAnswerField.animState = AnimationState::NONE;
        }
    }
}

void GUI::resetGame() {
    trainer.generateNewProblems();
    multAnswerField.text.clear();
    multAnswerField.pendingCheck = false;
    multAnswerField.animState = AnimationState::NONE;
    divAnswerField.text.clear();
    divAnswerField.pendingCheck = false;
    divAnswerField.animState = AnimationState::NONE;
}

void GUI::render() {
    SDL_SetRenderDrawColor(renderer, 
        Constants::BACKGROUND_COLOR.r,
        Constants::BACKGROUND_COLOR.g,
        Constants::BACKGROUND_COLOR.b,
        Constants::BACKGROUND_COLOR.a);
    SDL_RenderClear(renderer);

    if (currentScreen == Screen::SETUP) {
        renderSetup();
    } else {
        renderTraining();
    }

    SDL_RenderPresent(renderer);
}

void GUI::renderSetup() {
    renderTextCentered("EinMalEins Trainer - Setup", 20, Constants::TEXT_COLOR, font);
    renderTextCentered("Wahle die Einmaleins-Reihen und Multiplikatoren", 55, Constants::TEXT_COLOR, smallFont);

    renderText("Einmaleins-Reihen:", 50, 95, Constants::TEXT_COLOR, smallFont);

    auto& config = trainer.getConfig();

    int buttonSize = 55;
    int buttonSpacing = 12;
    int rowStartX = 50;

    for (int i = 0; i < 10; i++) {
        int num = i + 1;
        bool hasMultipliers = !config.multipliers[num].empty();
        SDL_Color color = hasMultipliers ? 
            Constants::SELECTED_COLOR : Constants::UNSELECTED_COLOR;
        
        SDL_Rect btn = {rowStartX + i * (buttonSize + buttonSpacing), 120, buttonSize, buttonSize};
        baseNumberButtons[i] = btn;
        
        SDL_SetRenderDrawColor(renderer, color.r, color.g, color.b, color.a);
        SDL_RenderFillRect(renderer, &btn);
        SDL_SetRenderDrawColor(renderer, 0, 0, 0, 255);
        SDL_RenderDrawRect(renderer, &btn);
        
        renderText(std::to_string(num), 
            btn.x + 18,
            btn.y + 15,
            Constants::TEXT_COLOR, font);
    }

    renderText("Multiplikatoren:", 50, 195, Constants::TEXT_COLOR, smallFont);

    if (selectedBaseForMultipliers > 0) {
        renderText(("fur " + std::to_string(selectedBaseForMultipliers) + "er Reihe:").c_str(), 
            200, 195, Constants::TEXT_COLOR, smallFont);

        int multStartX = 50;
        int multStartY = 225;
        int multButtonSize = 50;
        int multSpacing = 10;

        for (int j = 0; j < 10; j++) {
            int mult = j + 1;
            SDL_Color color = config.multipliers[selectedBaseForMultipliers].count(mult) ? 
                Constants::SELECTED_COLOR : Constants::UNSELECTED_COLOR;
            
            SDL_Rect btn = {multStartX + j * (multButtonSize + multSpacing), multStartY, multButtonSize, multButtonSize};
            
            SDL_SetRenderDrawColor(renderer, color.r, color.g, color.b, color.a);
            SDL_RenderFillRect(renderer, &btn);
            SDL_SetRenderDrawColor(renderer, 0, 0, 0, 255);
            SDL_RenderDrawRect(renderer, &btn);
            
            renderText(std::to_string(mult), 
                btn.x + 15,
                btn.y + 12,
                Constants::TEXT_COLOR, font);
        }
    } else {
        renderTextCentered("Klicke auf eine Reihe um die Multiplikatoren zu sehen", 240, Constants::TEXT_COLOR, smallFont);
    }

    bool hasSelection = !config.baseNumbers.empty();
    SDL_Color buttonColor = hasSelection ? Constants::BUTTON_COLOR : Constants::UNSELECTED_COLOR;
    SDL_SetRenderDrawColor(renderer, buttonColor.r, buttonColor.g, buttonColor.b, buttonColor.a);
    SDL_RenderFillRect(renderer, &startButton);
    SDL_SetRenderDrawColor(renderer, 255, 255, 255, 255);
    renderText("Training starten", startButton.x + 10, startButton.y + 15, {255, 255, 255, 255}, smallFont);
}

void GUI::renderTraining() {
    renderTextCentered("EinMalEins Trainer", 20, Constants::TEXT_COLOR, font);
    renderTextCentered("TAB = Feld wechseln", 55, Constants::TEXT_COLOR, smallFont);

    MathProblem mult = trainer.getMultiplicationProblem();
    MathProblem div = trainer.getDivisionProblem();

    if (trainer.hasValidMultiplicationProblem()) {
        renderRow(0, getRowY(0), mult, multAnswerField);
    }
    if (trainer.hasValidDivisionProblem()) {
        renderRow(1, getRowY(1), div, divAnswerField);
    }

    SDL_SetRenderDrawColor(renderer, Constants::BUTTON_COLOR.r, Constants::BUTTON_COLOR.g, Constants::BUTTON_COLOR.b, Constants::BUTTON_COLOR.a);
    SDL_RenderFillRect(renderer, &setupButton);
    renderText("Setup", setupButton.x + 45, setupButton.y + 8, {255, 255, 255, 255}, smallFont);
}

void GUI::renderRow(int row, int y, const MathProblem& problem, InputField& answerField) {
    SDL_Color bgColor = Constants::FIELD_BG_COLOR;
    SDL_Color textColor = Constants::TEXT_COLOR;

    if (answerField.animState != AnimationState::NONE) {
        Uint32 elapsed = SDL_GetTicks() - answerField.animStartTime;
        float progress = static_cast<float>(elapsed) / Constants::ANIMATION_DURATION_MS;
        
        if (progress > 1.0f) {
            progress = 2.0f - progress;
        }
        
        progress = std::min(1.0f, progress);
        
        float scale = 1.0f + 0.3f * progress;
        
        if (answerField.animState == AnimationState::SHRINKING) {
            scale = 1.0f - 0.3f * progress;
        }

        SDL_Color animColor = answerField.isCorrect ? 
            Constants::CORRECT_COLOR : Constants::WRONG_COLOR;

        int offsetX = (Constants::FIELD_WIDTH * (scale - 1)) / 2;
        int offsetY = (Constants::FIELD_HEIGHT * (scale - 1)) / 2;
        
        SDL_Rect scaledRect = {
            answerField.rect.x - offsetX,
            answerField.rect.y - offsetY,
            static_cast<int>(Constants::FIELD_WIDTH * scale),
            static_cast<int>(Constants::FIELD_HEIGHT * scale)
        };
        
        SDL_SetRenderDrawColor(renderer, animColor.r, animColor.g, animColor.b, animColor.a);
        SDL_RenderFillRect(renderer, &scaledRect);
        
        SDL_Color textColorAnim = {255, 255, 255, 255};
        std::string animText = answerField.text.empty() ? "?" : answerField.text;
        int animTextWidth, animTextHeight;
        TTF_SizeText(xlargeFont, animText.c_str(), &animTextWidth, &animTextHeight);
        renderText(animText,
            scaledRect.x + (scaledRect.w - animTextWidth) / 2,
            scaledRect.y + (scaledRect.h - animTextHeight) / 2,
            textColorAnim, xlargeFont);
    } else {
        renderText(std::to_string(problem.num1), getFieldX(0) + 30, y + 10, textColor, xlargeFont);
        renderText(std::string(1, problem.operation), getFieldX(1) + 35, y + 10, Constants::OPERATOR_COLOR, xlargeFont);
        renderText(std::to_string(problem.num2), getFieldX(2) + 30, y + 10, textColor, xlargeFont);
        renderText("=", getFieldX(3) + 35, y + 10, Constants::OPERATOR_COLOR, xlargeFont);
        
        SDL_Color inputBg = answerField.isActive ? 
            Constants::INPUT_BORDER_COLOR : Constants::INPUT_BG_COLOR;
        renderField(answerField.rect.x, y, 
            answerField.text.empty() ? "_" : answerField.text, 
            true, answerField.isActive, inputBg, textColor, 1.0f, xlargeFont);
    }
}

void GUI::renderField(int x, int y, const std::string& text, bool isInput, bool isActive, const SDL_Color& bgColor, const SDL_Color& textColor, float scale, TTF_Font* usedFont) {
    SDL_Rect rect = {x, y, 
        static_cast<int>(Constants::FIELD_WIDTH * scale), 
        static_cast<int>(Constants::FIELD_HEIGHT * scale)};

    SDL_SetRenderDrawColor(renderer, bgColor.r, bgColor.g, bgColor.b, bgColor.a);
    SDL_RenderFillRect(renderer, &rect);

    if (isInput && isActive) {
        SDL_SetRenderDrawColor(renderer, 0, 0, 255, 255);
        SDL_RenderDrawRect(renderer, &rect);
    }

    TTF_Font* fontToUse = usedFont ? usedFont : font;
    int textWidth, textHeight;
    TTF_SizeText(fontToUse, text.c_str(), &textWidth, &textHeight);

    int textX = rect.x + (rect.w - textWidth) / 2;
    int textY = rect.y + (rect.h - textHeight) / 2;
    renderText(text, textX, textY, textColor, usedFont);
}

void GUI::renderText(const std::string& text, int x, int y, const SDL_Color& color, TTF_Font* usedFont) {
    if (text.empty()) return;
    
    TTF_Font* fontToUse = usedFont ? usedFont : font;
    SDL_Surface* surface = TTF_RenderText_Blended(fontToUse, text.c_str(), color);
    
    if (!surface) return;

    SDL_Texture* texture = SDL_CreateTextureFromSurface(renderer, surface);
    SDL_FreeSurface(surface);

    if (!texture) return;

    SDL_Rect dstRect = {x, y, 0, 0};
    SDL_QueryTexture(texture, nullptr, nullptr, &dstRect.w, &dstRect.h);
    
    SDL_RenderCopy(renderer, texture, nullptr, &dstRect);
    SDL_DestroyTexture(texture);
}

void GUI::renderTextCentered(const std::string& text, int y, const SDL_Color& color, TTF_Font* usedFont) {
    if (text.empty()) return;
    
    TTF_Font* fontToUse = usedFont ? usedFont : font;
    SDL_Surface* surface = TTF_RenderText_Blended(fontToUse, text.c_str(), color);
    
    if (!surface) return;

    SDL_Texture* texture = SDL_CreateTextureFromSurface(renderer, surface);
    SDL_FreeSurface(surface);

    if (!texture) return;

    int textWidth;
    SDL_QueryTexture(texture, nullptr, nullptr, &textWidth, nullptr);
    
    int x = (Constants::WINDOW_WIDTH - textWidth) / 2;
    SDL_Rect dstRect = {x, y, 0, 0};
    SDL_QueryTexture(texture, nullptr, nullptr, &dstRect.w, &dstRect.h);
    
    SDL_RenderCopy(renderer, texture, nullptr, &dstRect);
    SDL_DestroyTexture(texture);
}
