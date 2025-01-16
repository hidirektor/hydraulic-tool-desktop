package com.hidirektor.dashboard.utils;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Utils {

    public static void clickButton(Button actionButton, int clickCount) {
        MouseEvent mousePressedEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, MouseButton.PRIMARY, clickCount, false, false, false, false, false, false, false, false, false, false, null);
        actionButton.fireEvent(mousePressedEvent);
    }
}
