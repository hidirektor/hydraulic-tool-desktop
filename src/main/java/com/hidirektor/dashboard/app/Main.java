package com.hidirektor.dashboard.app;

import com.hidirektor.dashboard.utils.SceneUtil;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Main extends Application {

    List<Screen> screens = Screen.getScreens();

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneUtil.openScreen(screens.get(0), "fxml/Landing.fxml", "/assets/images/logos/onderlift-hydraulic-logo.png");
    }

    public static void main(String[] args) {
        launch(args);
    }
}