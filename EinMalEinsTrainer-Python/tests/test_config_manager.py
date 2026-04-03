import pytest
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config_manager import save_config, load_config, get_config_path
from trainer import EinmaleinsConfig


class TestConfigManager:
    def test_save_and_load_config_roundtrip(self, tmp_path, monkeypatch):
        def mock_get_config_path():
            return str(tmp_path / "test_config.json")
        
        monkeypatch.setattr('config_manager.get_config_path', mock_get_config_path)
        
        config = EinmaleinsConfig.default()
        config.base_numbers = {1, 2, 3}
        config.multipliers[1] = {1, 2}
        config.multipliers[2] = {3, 4}
        config.multipliers[5] = set()
        
        result = save_config(config)
        assert result is True
        
        loaded = load_config()
        assert loaded is not None
        assert loaded.base_numbers == {1, 2, 3}
        assert loaded.multipliers[1] == {1, 2}
        assert loaded.multipliers[2] == {3, 4}

    def test_load_config_returns_default_when_file_not_exists(self, tmp_path, monkeypatch):
        def mock_get_config_path():
            return str(tmp_path / "nonexistent_config.json")
        
        monkeypatch.setattr('config_manager.get_config_path', mock_get_config_path)
        
        loaded = load_config()
        
        assert loaded is not None
        assert loaded.base_numbers == {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}

    def test_load_config_handles_invalid_json(self, tmp_path, monkeypatch):
        config_path = tmp_path / "invalid_config.json"
        config_path.write_text("invalid json content")
        
        def mock_get_config_path():
            return str(config_path)
        
        monkeypatch.setattr('config_manager.get_config_path', mock_get_config_path)
        
        loaded = load_config()
        
        assert loaded is not None
        assert loaded.base_numbers == {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}

    def test_load_config_partial_multipliers(self, tmp_path, monkeypatch):
        config_path = tmp_path / "partial_config.json"
        config_path.write_text('''{
            "baseNumbers": [1, 2, 5],
            "multipliers": {
                "1": [1, 2, 3],
                "2": [4, 5],
                "5": []
            }
        }''')
        
        def mock_get_config_path():
            return str(config_path)
        
        monkeypatch.setattr('config_manager.get_config_path', mock_get_config_path)
        
        loaded = load_config()
        
        assert loaded.base_numbers == {1, 2, 5}
        assert loaded.multipliers[1] == {1, 2, 3}
        assert loaded.multipliers[2] == {4, 5}
        assert loaded.multipliers[5] == set()
        assert loaded.multipliers[3] == {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}

    def test_get_config_path_linux(self, monkeypatch):
        monkeypatch.syspath_prepend(None)
        import config_manager
        
        path = config_manager.get_config_path()
        
        assert '.config' in path
        assert 'einmaleins_config' in path

    def test_save_config_creates_directory(self, tmp_path, monkeypatch):
        config_dir = tmp_path / "subdir" / "config"
        
        def mock_get_config_path():
            return str(config_dir / "config.json")
        
        monkeypatch.setattr('config_manager.get_config_path', mock_get_config_path)
        
        config = EinmaleinsConfig.default()
        result = save_config(config)
        
        assert result is True
        assert config_dir.exists()

    def test_save_config_returns_false_on_error(self, tmp_path, monkeypatch):
        def mock_get_config_path():
            return "/nonexistent/path/config.json"
        
        monkeypatch.setattr('config_manager.get_config_path', mock_get_config_path)
        
        config = EinmaleinsConfig.default()
        result = save_config(config)
        
        assert result is False
