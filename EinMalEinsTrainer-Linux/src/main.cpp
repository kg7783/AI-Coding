#include <iostream>
#include "GUI.h"

int main(int argc, char* argv[]) {
    GUI gui;
    
    if (!gui.init()) {
        std::cerr << "Failed to initialize GUI" << std::endl;
        return 1;
    }

    gui.run();
    
    return 0;
}
