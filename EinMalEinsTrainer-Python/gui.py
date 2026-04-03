import pygame
import sys
from enum import Enum
from typing import Optional
from trainer import Trainer, MathProblem
import constants


class AnimationState(Enum):
    NONE = 0
    GROWING = 1
    SHRINKING = 2


class Screen(Enum):
    SETUP = 0
    TRAINING = 1


class InputField:
    def __init__(self, rect: pygame.Rect):
        self.rect = rect
        self.text: str = ""
        self.anim_text: str = ""
        self.is_active: bool = False
        self.last_input_time: int = 0
        self.pending_check: bool = False
        self.is_correct: bool = False
        self.anim_state: AnimationState = AnimationState.NONE
        self.anim_start_time: int = 0
        self.scale: float = 1.0
        self.show_check: bool = False
        self.check_scale: float = 0.0
        self.show_x: bool = False
        self.x_scale: float = 0.0
        self.needs_new_problem: bool = False


class GUI:
    def __init__(self):
        pygame.init()
        
        self.window = pygame.display.set_mode((constants.WINDOW_WIDTH, constants.WINDOW_HEIGHT))
        pygame.display.set_caption(constants.WINDOW_TITLE)
        
        self.font = self.load_font(constants.FONT_SIZE)
        self.large_font = self.load_font(constants.FONT_SIZE_LARGE)
        self.xlarge_font = self.load_font(constants.FONT_SIZE_XLARGE)
        self.small_font = self.load_font(constants.FONT_SIZE_SMALL)
        
        self.trainer = Trainer()
        
        self.selected_field: int = -1
        self.current_screen: Screen = Screen.TRAINING
        self.selected_base_for_multipliers: int = 0
        
        start_x = (constants.WINDOW_WIDTH - (5 * constants.FIELD_WIDTH + 4 * constants.FIELD_SPACING + 60)) // 2
        self.mult_answer_field = InputField(pygame.Rect(
            start_x + 4 * (constants.FIELD_WIDTH + constants.FIELD_SPACING) + 60,
            constants.START_Y,
            constants.FIELD_WIDTH,
            constants.FIELD_HEIGHT
        ))
        self.div_answer_field = InputField(pygame.Rect(
            start_x + 4 * (constants.FIELD_WIDTH + constants.FIELD_SPACING) + 60,
            constants.START_Y + constants.FIELD_HEIGHT + constants.ROW_SPACING,
            constants.FIELD_WIDTH,
            constants.FIELD_HEIGHT
        ))
        
        self.base_number_buttons: list[pygame.Rect] = []
        button_size = 50
        button_spacing = 10
        base_start_x = 50
        base_start_y = 100
        
        for i in range(10):
            self.base_number_buttons.append(pygame.Rect(
                base_start_x + i * (button_size + button_spacing),
                base_start_y,
                button_size,
                button_size
            ))
        
        self.multiplier_buttons: list[list[pygame.Rect]] = []
        mult_start_x = 50
        mult_start_y = 180
        
        for base in range(10):
            row: list[pygame.Rect] = []
            for mult in range(10):
                row.append(pygame.Rect(
                    mult_start_x + mult * (button_size + 5),
                    mult_start_y + base * (button_size + 5),
                    button_size,
                    button_size
                ))
            self.multiplier_buttons.append(row)
        
        self.start_button = pygame.Rect(
            constants.WINDOW_WIDTH - 220,
            constants.WINDOW_HEIGHT - 80,
            200,
            50
        )
        self.setup_button = pygame.Rect(
            constants.WINDOW_WIDTH - 200,
            60,
            150,
            40
        )
        
        pygame.key.start_text_input()

    def load_font(self, size: int) -> pygame.font.Font:
        font_paths = [
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/TTF/DejaVuSans.ttf",
            "/usr/share/fonts/TTF/DejaVuSans-Bold.ttf",
            pygame.font.match_font("dejavusans"),
            pygame.font.match_font("sans"),
        ]
        
        for path in font_paths:
            if path:
                try:
                    return pygame.font.Font(path, size)
                except:
                    continue
        
        return pygame.font.Font(None, size)

    def get_field_x(self, col: int) -> int:
        start_x = (constants.WINDOW_WIDTH - (5 * constants.FIELD_WIDTH + 4 * constants.FIELD_SPACING + 60)) // 2
        return start_x + col * (constants.FIELD_WIDTH + constants.FIELD_SPACING)

    def get_row_y(self, row: int) -> int:
        return constants.START_Y + row * (constants.FIELD_HEIGHT + constants.ROW_SPACING)

    def run(self):
        clock = pygame.time.Clock()
        running = True
        
        while running:
            for event in pygame.event.get():
                self.handle_event(event)
                if event.type == pygame.QUIT:
                    running = False
            
            if self.current_screen == Screen.TRAINING:
                self.check_answers()
                self.update_animations()
            
            self.render()
            clock.tick(60)
        
        pygame.quit()
        sys.exit()

    def handle_event(self, event: pygame.event.Event):
        if event.type == pygame.KEYDOWN:
            self.handle_key_down(event)
        elif event.type == pygame.MOUSEBUTTONDOWN:
            if self.current_screen == Screen.SETUP:
                self.handle_setup_click(event.pos[0], event.pos[1])
            else:
                self.handle_mouse_down(event.pos[0], event.pos[1])
        elif event.type == pygame.TEXTINPUT:
            self.handle_text_input(event.text)

    def handle_key_down(self, event: pygame.event.Event):
        if self.current_screen == Screen.SETUP:
            if event.key == pygame.K_ESCAPE:
                self.current_screen = Screen.TRAINING
                self.trainer.generate_new_problems()
            return
        
        if event.key == pygame.K_TAB:
            self.selected_field = (self.selected_field + 1) % 2
            self.mult_answer_field.is_active = (self.selected_field == 0)
            self.div_answer_field.is_active = (self.selected_field == 1)
            return
        
        if event.key == pygame.K_BACKSPACE:
            if self.selected_field == 0 and self.mult_answer_field.text:
                self.mult_answer_field.text = self.mult_answer_field.text[:-1]
                self.trainer.set_multiplication_answer(self.mult_answer_field.text)
            elif self.selected_field == 1 and self.div_answer_field.text:
                self.div_answer_field.text = self.div_answer_field.text[:-1]
                self.trainer.set_division_answer(self.div_answer_field.text)
            return
        
        if event.key == pygame.K_RETURN:
            if self.selected_field == 0:
                self.mult_answer_field.last_input_time = pygame.time.get_ticks()
                self.mult_answer_field.pending_check = True
            elif self.selected_field == 1:
                self.div_answer_field.last_input_time = pygame.time.get_ticks()
                self.div_answer_field.pending_check = True
            return

    def handle_setup_click(self, mx: int, my: int):
        point = pygame.Rect(mx, my, 1, 1)
        
        for i, btn in enumerate(self.base_number_buttons):
            if btn.collidepoint(mx, my):
                num = i + 1
                config = self.trainer.get_config()
                has_multipliers = bool(config.multipliers[num])
                self.selected_base_for_multipliers = num
                return
        
        if self.selected_base_for_multipliers > 0:
            mult_start_x = 50
            mult_start_y = 225
            button_size = 50
            button_spacing = 10
            
            for j in range(10):
                btn = pygame.Rect(
                    mult_start_x + j * (button_size + button_spacing),
                    mult_start_y,
                    button_size,
                    button_size
                )
                if btn.collidepoint(mx, my):
                    mult = j + 1
                    config = self.trainer.get_config()
                    if mult in config.multipliers[self.selected_base_for_multipliers]:
                        self.trainer.set_multiplier_selected(self.selected_base_for_multipliers, mult, False)
                    else:
                        self.trainer.set_multiplier_selected(self.selected_base_for_multipliers, mult, True)
                    return
        
        if self.start_button.collidepoint(mx, my):
            if self.trainer.get_config().base_numbers:
                self.current_screen = Screen.TRAINING
                self.trainer.generate_new_problems()

    def handle_mouse_down(self, mx: int, my: int):
        if self.setup_button.collidepoint(mx, my):
            self.current_screen = Screen.SETUP
            self.trainer.generate_new_problems()
            
            config = self.trainer.get_config()
            self.selected_base_for_multipliers = 0
            for i in range(1, 11):
                if config.multipliers[i]:
                    self.selected_base_for_multipliers = i
                    break
            return
        
        self.mult_answer_field.is_active = self.mult_answer_field.rect.collidepoint(mx, my)
        self.div_answer_field.is_active = self.div_answer_field.rect.collidepoint(mx, my)
        
        if self.mult_answer_field.is_active:
            self.selected_field = 0
        elif self.div_answer_field.is_active:
            self.selected_field = 1

    def handle_text_input(self, text: str):
        if self.selected_field == 0 and len(self.mult_answer_field.text) < 10:
            self.mult_answer_field.text += text
            self.mult_answer_field.last_input_time = pygame.time.get_ticks()
            self.mult_answer_field.pending_check = True
            self.trainer.set_multiplication_answer(self.mult_answer_field.text)
        elif self.selected_field == 1 and len(self.div_answer_field.text) < 10:
            self.div_answer_field.text += text
            self.div_answer_field.last_input_time = pygame.time.get_ticks()
            self.div_answer_field.pending_check = True
            self.trainer.set_division_answer(self.div_answer_field.text)

    def check_answers(self):
        current_time = pygame.time.get_ticks()
        
        if self.mult_answer_field.pending_check and \
           current_time - self.mult_answer_field.last_input_time > constants.INPUT_DELAY_MS:
            
            self.mult_answer_field.pending_check = False
            self.mult_answer_field.is_correct = self.trainer.is_multiplication_correct()
            
            if self.mult_answer_field.anim_state == AnimationState.NONE:
                self.mult_answer_field.anim_text = self.mult_answer_field.text
                if self.mult_answer_field.is_correct:
                    self.mult_answer_field.anim_state = AnimationState.GROWING
                    self.mult_answer_field.show_check = True
                    self.mult_answer_field.check_scale = 0.0
                    self.mult_answer_field.needs_new_problem = True
                else:
                    self.mult_answer_field.anim_state = AnimationState.SHRINKING
                    self.mult_answer_field.show_x = True
                    self.mult_answer_field.x_scale = 0.0
                self.mult_answer_field.anim_start_time = current_time
            
            if not self.mult_answer_field.is_correct and self.mult_answer_field.text:
                self.mult_answer_field.text = ""
        
        if self.div_answer_field.pending_check and \
           current_time - self.div_answer_field.last_input_time > constants.INPUT_DELAY_MS:
            
            self.div_answer_field.pending_check = False
            self.div_answer_field.is_correct = self.trainer.is_division_correct()
            
            if self.div_answer_field.anim_state == AnimationState.NONE:
                self.div_answer_field.anim_text = self.div_answer_field.text
                if self.div_answer_field.is_correct:
                    self.div_answer_field.anim_state = AnimationState.GROWING
                    self.div_answer_field.show_check = True
                    self.div_answer_field.check_scale = 0.0
                    self.div_answer_field.needs_new_problem = True
                else:
                    self.div_answer_field.anim_state = AnimationState.SHRINKING
                    self.div_answer_field.show_x = True
                    self.div_answer_field.x_scale = 0.0
                self.div_answer_field.anim_start_time = current_time
            
            if not self.div_answer_field.is_correct and self.div_answer_field.text:
                self.div_answer_field.text = ""

    def update_animations(self):
        current_time = pygame.time.get_ticks()
        
        if self.mult_answer_field.anim_state != AnimationState.NONE:
            elapsed = current_time - self.mult_answer_field.anim_start_time
            
            if self.mult_answer_field.is_correct:
                if elapsed < 300:
                    self.mult_answer_field.scale = 1.0 + 0.3 * (elapsed / 300.0)
                elif elapsed < 600:
                    self.mult_answer_field.scale = 1.3 - 0.3 * ((elapsed - 300) / 300.0)
                else:
                    self.mult_answer_field.scale = 1.0
                
                if elapsed < 500:
                    self.mult_answer_field.check_scale = (elapsed / 500.0) * 2.0
                elif elapsed < 1000:
                    self.mult_answer_field.check_scale = 2.0 - ((elapsed - 500) / 500.0) * 2.0
                else:
                    if self.mult_answer_field.needs_new_problem:
                        self.trainer.generate_multiplication_problem()
                        self.mult_answer_field.text = ""
                        self.mult_answer_field.anim_text = ""
                        self.mult_answer_field.needs_new_problem = False
                    self.mult_answer_field.show_check = False
                    self.mult_answer_field.check_scale = 0.0
                    self.mult_answer_field.anim_state = AnimationState.NONE
            else:
                if elapsed < 300:
                    self.mult_answer_field.scale = 1.0 - 0.3 * (elapsed / 300.0)
                elif elapsed < 600:
                    self.mult_answer_field.scale = 0.7 + 0.3 * ((elapsed - 300) / 300.0)
                else:
                    self.mult_answer_field.scale = 1.0
                
                if elapsed < 500:
                    self.mult_answer_field.x_scale = (elapsed / 500.0) * 2.0
                elif elapsed < 1000:
                    self.mult_answer_field.x_scale = 2.0 - ((elapsed - 500) / 500.0) * 2.0
                else:
                    self.mult_answer_field.show_x = False
                    self.mult_answer_field.x_scale = 0.0
                    self.mult_answer_field.text = ""
                    self.mult_answer_field.anim_state = AnimationState.NONE
        
        if self.div_answer_field.anim_state != AnimationState.NONE:
            elapsed = current_time - self.div_answer_field.anim_start_time
            
            if self.div_answer_field.is_correct:
                if elapsed < 300:
                    self.div_answer_field.scale = 1.0 + 0.3 * (elapsed / 300.0)
                elif elapsed < 600:
                    self.div_answer_field.scale = 1.3 - 0.3 * ((elapsed - 300) / 300.0)
                else:
                    self.div_answer_field.scale = 1.0
                
                if elapsed < 500:
                    self.div_answer_field.check_scale = (elapsed / 500.0) * 2.0
                elif elapsed < 1000:
                    self.div_answer_field.check_scale = 2.0 - ((elapsed - 500) / 500.0) * 2.0
                else:
                    if self.div_answer_field.needs_new_problem:
                        self.trainer.generate_division_problem()
                        self.div_answer_field.text = ""
                        self.div_answer_field.anim_text = ""
                        self.div_answer_field.needs_new_problem = False
                    self.div_answer_field.show_check = False
                    self.div_answer_field.check_scale = 0.0
                    self.div_answer_field.anim_state = AnimationState.NONE
            else:
                if elapsed < 300:
                    self.div_answer_field.scale = 1.0 - 0.3 * (elapsed / 300.0)
                elif elapsed < 600:
                    self.div_answer_field.scale = 0.7 + 0.3 * ((elapsed - 300) / 300.0)
                else:
                    self.div_answer_field.scale = 1.0
                
                if elapsed < 500:
                    self.div_answer_field.x_scale = (elapsed / 500.0) * 2.0
                elif elapsed < 1000:
                    self.div_answer_field.x_scale = 2.0 - ((elapsed - 500) / 500.0) * 2.0
                else:
                    self.div_answer_field.show_x = False
                    self.div_answer_field.x_scale = 0.0
                    self.div_answer_field.text = ""
                    self.div_answer_field.anim_state = AnimationState.NONE

    def reset_game(self):
        self.trainer.generate_new_problems()
        self.mult_answer_field.text = ""
        self.mult_answer_field.anim_text = ""
        self.mult_answer_field.pending_check = False
        self.mult_answer_field.anim_state = AnimationState.NONE
        self.mult_answer_field.scale = 1.0
        self.mult_answer_field.show_check = False
        self.mult_answer_field.check_scale = 0.0
        self.mult_answer_field.show_x = False
        self.mult_answer_field.x_scale = 0.0
        self.mult_answer_field.needs_new_problem = False
        self.div_answer_field.text = ""
        self.div_answer_field.anim_text = ""
        self.div_answer_field.pending_check = False
        self.div_answer_field.anim_state = AnimationState.NONE
        self.div_answer_field.scale = 1.0
        self.div_answer_field.show_check = False
        self.div_answer_field.check_scale = 0.0
        self.div_answer_field.show_x = False
        self.div_answer_field.x_scale = 0.0
        self.div_answer_field.needs_new_problem = False

    def render(self):
        self.window.fill(constants.BACKGROUND_COLOR)
        
        if self.current_screen == Screen.SETUP:
            self.render_setup()
        else:
            self.render_training()
        
        pygame.display.flip()

    def render_setup(self):
        self.render_text_centered("EinMalEins Trainer - Setup", 20, constants.TEXT_COLOR, self.font)
        self.render_text_centered("Wahle die Einmaleins-Reihen und Multiplikatoren", 55, constants.TEXT_COLOR, self.small_font)
        
        self.render_text("Einmaleins-Reihen:", 50, 95, constants.TEXT_COLOR, self.small_font)
        
        config = self.trainer.get_config()
        
        button_size = 55
        button_spacing = 12
        row_start_x = 50
        
        for i in range(10):
            num = i + 1
            has_multipliers = bool(config.multipliers[num])
            num_multipliers = len(config.multipliers[num])
            is_partial = 0 < num_multipliers < 10
            is_selected = (num == self.selected_base_for_multipliers)
            
            btn = pygame.Rect(
                row_start_x + i * (button_size + button_spacing),
                120,
                button_size,
                button_size
            )
            self.base_number_buttons[i] = btn
            
            if is_partial:
                pygame.draw.polygon(self.window, constants.SELECTED_COLOR, 
                    [(btn.left, btn.bottom), (btn.left, btn.top), (btn.right, btn.top)])
                pygame.draw.polygon(self.window, constants.PARTIAL_COLOR, 
                    [(btn.left, btn.bottom), (btn.right, btn.top), (btn.right, btn.bottom)])
            elif has_multipliers:
                pygame.draw.rect(self.window, constants.SELECTED_COLOR, btn)
            else:
                pygame.draw.rect(self.window, constants.UNSELECTED_COLOR, btn)
            
            if is_selected:
                pygame.draw.rect(self.window, (0, 0, 255), btn, 4)
            else:
                pygame.draw.rect(self.window, (0, 0, 0), btn, 1)
            
            text_surf = self.font.render(str(num), True, constants.TEXT_COLOR)
            text_rect = text_surf.get_rect(center=btn.center)
            self.window.blit(text_surf, text_rect)
        
        self.render_text("Multiplikatoren:", 50, 195, constants.TEXT_COLOR, self.small_font)
        
        if self.selected_base_for_multipliers > 0:
            self.render_text(f"fur {self.selected_base_for_multipliers}er Reihe:", 200, 195, constants.TEXT_COLOR, self.small_font)
            
            mult_start_x = 50
            mult_start_y = 225
            mult_button_size = 50
            mult_spacing = 10
            
            for j in range(10):
                mult = j + 1
                color = constants.SELECTED_COLOR if mult in config.multipliers[self.selected_base_for_multipliers] else constants.UNSELECTED_COLOR
                
                btn = pygame.Rect(
                    mult_start_x + j * (mult_button_size + mult_spacing),
                    mult_start_y,
                    mult_button_size,
                    mult_button_size
                )
                
                pygame.draw.rect(self.window, color, btn)
                pygame.draw.rect(self.window, (0, 0, 0), btn, 1)
                
                text_surf = self.font.render(str(mult), True, constants.TEXT_COLOR)
                text_rect = text_surf.get_rect(center=btn.center)
                self.window.blit(text_surf, text_rect)
        else:
            self.render_text_centered("Klicke auf eine Reihe um die Multiplikatoren zu sehen", 240, constants.TEXT_COLOR, self.small_font)
        
        has_selection = bool(config.base_numbers)
        button_color = constants.BUTTON_COLOR if has_selection else constants.UNSELECTED_COLOR
        pygame.draw.rect(self.window, button_color, self.start_button)
        
        text_surf = self.small_font.render("Training starten", True, (255, 255, 255))
        text_rect = text_surf.get_rect(center=self.start_button.center)
        self.window.blit(text_surf, text_rect)

    def render_training(self):
        self.render_text_centered("EinMalEins Trainer", 20, constants.TEXT_COLOR, self.font)
        self.render_text_centered("TAB = Feld wechseln", 55, constants.TEXT_COLOR, self.small_font)
        
        mult = self.trainer.get_multiplication_problem()
        div = self.trainer.get_division_problem()
        
        if self.trainer.has_valid_multiplication_problem():
            self.render_row(0, self.get_row_y(0), mult, self.mult_answer_field)
        if self.trainer.has_valid_division_problem():
            self.render_row(1, self.get_row_y(1), div, self.div_answer_field)
        
        pygame.draw.rect(self.window, constants.BUTTON_COLOR, self.setup_button)
        
        text_surf = self.small_font.render("Setup", True, (255, 255, 255))
        text_rect = text_surf.get_rect(center=self.setup_button.center)
        self.window.blit(text_surf, text_rect)

    def render_row(self, row: int, y: int, problem: Optional[MathProblem], answer_field: InputField):
        if not problem:
            return
        
        self.render_text(str(problem.num1), self.get_field_x(0) + 30, y + 10, constants.TEXT_COLOR, self.xlarge_font)
        self.render_text(problem.operation, self.get_field_x(1) + 35, y + 10, constants.OPERATOR_COLOR, self.xlarge_font)
        self.render_text(str(problem.num2), self.get_field_x(2) + 30, y + 10, constants.TEXT_COLOR, self.xlarge_font)
        self.render_text("=", self.get_field_x(3) + 35, y + 10, constants.OPERATOR_COLOR, self.xlarge_font)
        
        if answer_field.anim_state != AnimationState.NONE:
            anim_color = constants.CORRECT_COLOR if answer_field.is_correct else constants.WRONG_COLOR
            
            scaled_rect = pygame.Rect(
                answer_field.rect.x - int(constants.FIELD_WIDTH * (answer_field.scale - 1) / 2),
                answer_field.rect.y - int(constants.FIELD_HEIGHT * (answer_field.scale - 1) / 2),
                int(constants.FIELD_WIDTH * answer_field.scale),
                int(constants.FIELD_HEIGHT * answer_field.scale)
            )
            
            pygame.draw.rect(self.window, anim_color, scaled_rect)
            
            anim_text = answer_field.anim_text if answer_field.anim_text else "?"
            text_surf = self.xlarge_font.render(anim_text, True, (255, 255, 255))
            text_rect = text_surf.get_rect(center=scaled_rect.center)
            self.window.blit(text_surf, text_rect)
            
            if answer_field.show_check:
                self.draw_check(scaled_rect.centerx, scaled_rect.centery, scaled_rect.height * 0.8, answer_field.check_scale)
            elif answer_field.show_x:
                self.draw_x(scaled_rect.centerx, scaled_rect.centery, scaled_rect.height * 0.8, answer_field.x_scale)
        else:
            input_bg = constants.INPUT_BORDER_COLOR if answer_field.is_active else constants.INPUT_BG_COLOR
            self.render_field(answer_field.rect.x, y, answer_field.text if answer_field.text else "_", True, answer_field.is_active, input_bg, constants.TEXT_COLOR, 1.0, self.xlarge_font)

    def draw_check(self, cx: int, cy: int, size: float, scale: float):
        if scale <= 0:
            return
        s = size * scale
        color = constants.CORRECT_COLOR
        width = max(2, int(8 * scale))
        
        pygame.draw.line(self.window, color, (cx - s * 0.15, cy + s * 0.05), (cx - s * 0.02, cy + s * 0.20), width)
        pygame.draw.line(self.window, color, (cx - s * 0.02, cy + s * 0.20), (cx + s * 0.18, cy - s * 0.15), width)

    def draw_x(self, cx: int, cy: int, size: float, scale: float):
        if scale <= 0:
            return
        s = size * scale
        color = constants.WRONG_COLOR
        width = max(2, int(8 * scale))
        
        pygame.draw.line(self.window, color, (cx - s * 0.15, cy - s * 0.15), (cx + s * 0.15, cy + s * 0.15), width)
        pygame.draw.line(self.window, color, (cx - s * 0.15, cy + s * 0.15), (cx + s * 0.15, cy - s * 0.15), width)

    def render_field(self, x: int, y: int, text: str, is_input: bool, is_active: bool, bg_color: pygame.Color, text_color: pygame.Color, scale: float = 1.0, used_font: Optional[pygame.font.Font] = None):
        rect = pygame.Rect(
            x,
            y,
            int(constants.FIELD_WIDTH * scale),
            int(constants.FIELD_HEIGHT * scale)
        )
        
        pygame.draw.rect(self.window, bg_color, rect)
        
        if is_input and is_active:
            pygame.draw.rect(self.window, (0, 0, 255), rect, 2)
        
        font = used_font if used_font else self.font
        text_surf = font.render(text, True, text_color)
        text_rect = text_surf.get_rect(center=rect.center)
        self.window.blit(text_surf, text_rect)

    def render_text(self, text: str, x: int, y: int, color: pygame.Color, used_font: Optional[pygame.font.Font] = None):
        if not text:
            return
        
        font = used_font if used_font else self.font
        text_surf = font.render(text, True, color)
        self.window.blit(text_surf, (x, y))

    def render_text_centered(self, text: str, y: int, color: pygame.Color, used_font: Optional[pygame.font.Font] = None):
        if not text:
            return
        
        font = used_font if used_font else self.font
        text_surf = font.render(text, True, color)
        text_rect = text_surf.get_rect(center=(constants.WINDOW_WIDTH // 2, y))
        self.window.blit(text_surf, text_rect)
