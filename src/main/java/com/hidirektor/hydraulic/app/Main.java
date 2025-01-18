package com.hidirektor.hydraulic.app;

import com.hidirektor.hydraulic.utils.File.FileUtility;
import com.hidirektor.hydraulic.utils.SceneUtil;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import me.t3sl4.util.os.OSUtil;

import java.io.IOException;
import java.util.List;

public class Main extends Application {

    List<Screen> screens = Screen.getScreens();

    @Override
    public void start(Stage primaryStage) throws IOException {
        int displayCount = screens.size();

        FileUtility.criticalFileSystem();

        /*if(displayCount > 1) {

        } else {
            SceneUtil.openScreen(screens.get(0), "fxml/Landing.fxml", "/assets/images/logos/onderlift-hydraulic-logo.png");
        }*/
        SceneUtil.openScreen(screens.get(0), "fxml/Landing.fxml", "/assets/images/logos/onderlift-hydraulic-logo.png");

        OSUtil.updateLocalVersion(SystemDefaults.PREF_NODE_NAME, SystemDefaults.PREF_UPDATER_KEY, SystemDefaults.getCurrentVersion());

        Thread systemThread = new Thread(FileUtility::setupLocalData);
        systemThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}