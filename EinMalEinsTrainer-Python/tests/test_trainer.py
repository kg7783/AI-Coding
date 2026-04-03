import pytest
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from trainer import MathProblem, EinmaleinsConfig, Trainer
import config_manager


class TestMathProblem:
    def test_creation(self):
        problem = MathProblem(num1=3, num2=4, result=12, operation='*')
        assert problem.num1 == 3
        assert problem.num2 == 4
        assert problem.result == 12
        assert problem.operation == '*'

    def test_division_problem(self):
        problem = MathProblem(num1=20, num2=4, result=5, operation='/')
        assert problem.num1 == 20
        assert problem.num2 == 4
        assert problem.result == 5
        assert problem.operation == '/'


class TestEinmaleinsConfig:
    def test_default_config(self):
        config = EinmaleinsConfig.default()
        
        assert config.base_numbers == {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
        
        for i in range(1, 11):
            assert config.multipliers[i] == {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}

    def test_custom_config(self):
        config = EinmaleinsConfig(
            base_numbers={1, 2, 3},
            multipliers=[set() for _ in range(11)]
        )
        config.multipliers[1] = {1, 2, 3}
        config.multipliers[2] = {4, 5}
        
        assert config.base_numbers == {1, 2, 3}
        assert config.multipliers[1] == {1, 2, 3}
        assert config.multipliers[2] == {4, 5}


def create_test_trainer():
    import random
    trainer = Trainer.__new__(Trainer)
    trainer.rng = random.Random(42)
    trainer.mult_factor1 = 0
    trainer.mult_factor2 = 0
    trainer.div_product = 0
    trainer.div_divisor = 0
    trainer.mult_answer = ""
    trainer.div_answer = ""
    trainer.has_valid_multiplication = False
    trainer.has_valid_division = False
    trainer.mult_checked = False
    trainer.div_checked = False
    trainer.config = EinmaleinsConfig.default()
    return trainer


class TestTrainerMultiplication:
    def test_generate_multiplication_problem_creates_valid_problem(self):
        trainer = create_test_trainer()
        trainer.generate_multiplication_problem()
        
        assert trainer.has_valid_multiplication
        problem = trainer.get_multiplication_problem()
        assert problem is not None
        assert problem.result == problem.num1 * problem.num2

    def test_multiplication_correct_answer(self):
        trainer = create_test_trainer()
        trainer.generate_multiplication_problem()
        problem = trainer.get_multiplication_problem()
        
        trainer.set_multiplication_answer(str(problem.result))
        
        assert trainer.is_multiplication_correct()

    def test_multiplication_wrong_answer(self):
        trainer = create_test_trainer()
        trainer.generate_multiplication_problem()
        
        trainer.set_multiplication_answer("999")
        
        assert not trainer.is_multiplication_correct()

    def test_multiplication_empty_answer(self):
        trainer = create_test_trainer()
        trainer.generate_multiplication_problem()
        trainer.set_multiplication_answer("")
        
        assert not trainer.is_multiplication_correct()

    def test_multiplication_invalid_answer(self):
        trainer = create_test_trainer()
        trainer.generate_multiplication_problem()
        trainer.set_multiplication_answer("abc")
        
        assert not trainer.is_multiplication_correct()


class TestTrainerDivision:
    def test_generate_division_problem_creates_valid_problem(self):
        trainer = create_test_trainer()
        trainer.generate_division_problem()
        
        assert trainer.has_valid_division
        problem = trainer.get_division_problem()
        assert problem is not None
        assert problem.result == problem.num1 // problem.num2

    def test_division_correct_answer(self):
        trainer = create_test_trainer()
        trainer.generate_division_problem()
        problem = trainer.get_division_problem()
        
        trainer.set_division_answer(str(problem.result))
        
        assert trainer.is_division_correct()

    def test_division_wrong_answer(self):
        trainer = create_test_trainer()
        trainer.generate_division_problem()
        
        trainer.set_division_answer("999")
        
        assert not trainer.is_division_correct()


class TestTrainerConfig:
    def test_set_base_number_selected_adds_number(self):
        trainer = create_test_trainer()
        trainer.config = EinmaleinsConfig.default()
        
        trainer.set_base_number_selected(5, True)
        
        assert 5 in trainer.get_config().base_numbers

    def test_set_base_number_selected_removes_number(self):
        trainer = create_test_trainer()
        trainer.config = EinmaleinsConfig.default()
        
        trainer.set_base_number_selected(5, True)
        trainer.set_base_number_selected(5, False)
        
        assert 5 not in trainer.get_config().base_numbers

    def test_set_multiplier_selected_adds_multiplier(self):
        trainer = create_test_trainer()
        trainer.config = EinmaleinsConfig.default()
        
        trainer.set_multiplier_selected(3, 7, True)
        
        assert 7 in trainer.get_config().multipliers[3]

    def test_set_multiplier_selected_removes_multiplier(self):
        trainer = create_test_trainer()
        trainer.config = EinmaleinsConfig.default()
        
        trainer.set_multiplier_selected(3, 7, True)
        trainer.set_multiplier_selected(3, 7, False)
        
        assert 7 not in trainer.get_config().multipliers[3]

    def test_invalid_base_number_ignored(self):
        trainer = create_test_trainer()
        trainer.config = EinmaleinsConfig.default()
        
        trainer.set_base_number_selected(15, True)
        
        assert 15 not in trainer.get_config().base_numbers

    def test_invalid_multiplier_ignored(self):
        trainer = create_test_trainer()
        trainer.config = EinmaleinsConfig.default()
        
        trainer.set_multiplier_selected(5, 15, True)
        
        assert 15 not in trainer.get_config().multipliers[5]


class TestTrainerClearAnswers:
    def test_clear_answers(self):
        trainer = create_test_trainer()
        
        trainer.set_multiplication_answer("42")
        trainer.set_division_answer("7")
        
        trainer.clear_answers()
        
        assert trainer.mult_answer == ""
        assert trainer.div_answer == ""
