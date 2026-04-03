import json
import os
import sys
from typing import Optional
from trainer import EinmaleinsConfig


def get_config_path() -> str:
    if sys.platform == "win32":
        appdata = os.getenv("APPDATA")
        if appdata:
            return os.path.join(appdata, "EinMalEinsTrainer", "config.json")
        return "config.json"
    else:
        home = os.path.expanduser("~")
        return os.path.join(home, ".config", "einmaleins_config")


def save_config(config: EinmaleinsConfig) -> bool:
    try:
        path = get_config_path()
        dir_path = os.path.dirname(path)
        
        if dir_path:
            os.makedirs(dir_path, exist_ok=True)
        
        multipliers_dict = {}
        for i in range(1, 11):
            multipliers_dict[str(i)] = sorted(list(config.multipliers[i]))
        
        data = {
            "baseNumbers": sorted(list(config.base_numbers)),
            "multipliers": multipliers_dict
        }
        
        with open(path, 'w') as f:
            json.dump(data, f, indent=2)
        return True
    except Exception as e:
        print(f"Error saving config: {e}")
        return False


def load_config() -> Optional[EinmaleinsConfig]:
    try:
        path = get_config_path()
        
        if not os.path.exists(path):
            return EinmaleinsConfig.default()
        
        with open(path, 'r') as f:
            data = json.load(f)
        
        base_numbers = set(data.get("baseNumbers", list(range(1, 11))))
        
        multipliers_data = data.get("multipliers", {})
        multipliers = [set() for _ in range(11)]
        
        for i in range(1, 11):
            key = str(i)
            if key in multipliers_data:
                multipliers[i] = set(multipliers_data[key])
            else:
                multipliers[i] = set(range(1, 11))
        
        return EinmaleinsConfig(base_numbers=base_numbers, multipliers=multipliers)
    except Exception as e:
        print(f"Error loading config: {e}")
        return EinmaleinsConfig.default()
