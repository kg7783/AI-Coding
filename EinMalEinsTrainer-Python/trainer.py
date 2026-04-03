import random
from dataclasses import dataclass
from typing import Set, List, Optional


@dataclass
class MathProblem:
    num1: int
    num2: int
    result: int
    operation: str


@dataclass
class EinmaleinsConfig:
    base_numbers: Set[int]
    multipliers: List[Set[int]]

    @staticmethod
    def default() -> 'EinmaleinsConfig':
        multipliers = [set() for _ in range(11)]
        for i in range(1, 11):
            multipliers[i] = set(range(1, 11))
        return EinmaleinsConfig(base_numbers=set(range(1, 11)), multipliers=multipliers)


class Trainer:
    def __init__(self):
        self.rng = random.Random()
        
        self.mult_factor1: int = 0
        self.mult_factor2: int = 0
        self.div_product: int = 0
        self.div_divisor: int = 0

        self.mult_answer: str = ""
        self.div_answer: str = ""

        self.has_valid_multiplication: bool = False
        self.has_valid_division: bool = False

        self.mult_checked: bool = False
        self.div_checked: bool = False

        self.config: EinmaleinsConfig = EinmaleinsConfig.default()
        
        self.load_config()
        self.generate_new_problems()

    def load_config(self):
        from config_manager import load_config
        loaded = load_config()
        if loaded:
            self.config = loaded

    def save_config(self):
        from config_manager import save_config
        save_config(self.config)

    def select_random_from_set(self, s: Set[int]) -> int:
        if not s:
            return 1
        return self.rng.choice(sorted(s))

    def generate_new_problems(self):
        self.has_valid_multiplication = False
        self.has_valid_division = False

        valid_multipliers: Set[int] = set()
        for i in range(1, 11):
            if self.config.multipliers[i]:
                valid_multipliers.add(i)

        if not valid_multipliers or not self.config.base_numbers:
            self.clear_answers()
            return

        mult1 = self.select_random_from_set(valid_multipliers)
        base1 = self.select_random_from_set(self.config.multipliers[mult1])

        mult2 = self.select_random_from_set(valid_multipliers)
        base2 = self.select_random_from_set(self.config.multipliers[mult2])

        self.has_valid_multiplication = True
        self.has_valid_division = True
        self.mult_factor1 = base1
        self.mult_factor2 = mult1
        self.div_product = base2 * mult2
        self.div_divisor = mult2

        self.clear_answers()

    def generate_multiplication_problem(self):
        self.has_valid_multiplication = False

        valid_multipliers: Set[int] = set()
        for i in range(1, 11):
            if self.config.multipliers[i]:
                valid_multipliers.add(i)

        if not valid_multipliers or not self.config.base_numbers:
            return

        mult = self.select_random_from_set(valid_multipliers)
        base = self.select_random_from_set(self.config.multipliers[mult])

        self.has_valid_multiplication = True
        self.mult_factor1 = base
        self.mult_factor2 = mult
        self.mult_answer = ""

    def generate_division_problem(self):
        self.has_valid_division = False

        valid_multipliers: Set[int] = set()
        for i in range(1, 11):
            if self.config.multipliers[i]:
                valid_multipliers.add(i)

        if not valid_multipliers or not self.config.base_numbers:
            return

        mult = self.select_random_from_set(valid_multipliers)
        base = self.select_random_from_set(self.config.multipliers[mult])

        self.has_valid_division = True
        self.div_product = base * mult
        self.div_divisor = mult
        self.div_answer = ""

    def get_multiplication_problem(self) -> Optional[MathProblem]:
        if not self.has_valid_multiplication:
            return None
        return MathProblem(
            num1=self.mult_factor1,
            num2=self.mult_factor2,
            result=self.mult_factor1 * self.mult_factor2,
            operation='*'
        )

    def get_division_problem(self) -> Optional[MathProblem]:
        if not self.has_valid_division:
            return None
        return MathProblem(
            num1=self.div_product,
            num2=self.div_divisor,
            result=self.div_product // self.div_divisor,
            operation='/'
        )

    def set_multiplication_answer(self, answer: str):
        self.mult_answer = answer

    def set_division_answer(self, answer: str):
        self.div_answer = answer

    def is_multiplication_correct(self) -> bool:
        if not self.has_valid_multiplication or not self.mult_answer:
            return False
        problem = self.get_multiplication_problem()
        if not problem:
            return False
        try:
            return int(self.mult_answer) == problem.result
        except ValueError:
            return False

    def is_division_correct(self) -> bool:
        if not self.has_valid_division or not self.div_answer:
            return False
        problem = self.get_division_problem()
        if not problem:
            return False
        try:
            return int(self.div_answer) == problem.result
        except ValueError:
            return False

    def was_multiplication_checked(self) -> bool:
        return self.mult_checked

    def was_division_checked(self) -> bool:
        return self.div_checked

    def has_valid_multiplication_problem(self) -> bool:
        return self.has_valid_multiplication

    def has_valid_division_problem(self) -> bool:
        return self.has_valid_division

    def clear_answers(self):
        self.mult_answer = ""
        self.div_answer = ""

    def get_config(self) -> EinmaleinsConfig:
        return self.config

    def set_base_number_selected(self, num: int, selected: bool):
        if num < 1 or num > 10:
            return
        if selected:
            self.config.base_numbers.add(num)
        else:
            self.config.base_numbers.discard(num)
        self.save_config()

    def set_multiplier_selected(self, multiplier: int, base: int, selected: bool):
        if multiplier < 1 or multiplier > 10 or base < 1 or base > 10:
            return
        if selected:
            self.config.multipliers[multiplier].add(base)
        else:
            self.config.multipliers[multiplier].discard(base)
            if not self.config.multipliers[multiplier]:
                self.config.base_numbers.discard(multiplier)
        self.save_config()
