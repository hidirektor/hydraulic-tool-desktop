package com.hidirektor.dashboard.utils;

import com.hidirektor.dashboard.Launcher;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class SceneUtil {

    public static double x, y;

    public static void openScreen(Screen currentScreen, String fxmlPath, String logoPath) throws IOException {
        Stage primaryStage = new Stage();
        Parent root = FXMLLoader.load(Objects.requireNonNull(Launcher.class.getResource(fxmlPath)));

        Scene scene = new Scene(root);
        scene.setFill(null);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.setTitle("Desktop Dashboard " + SystemDefaults.getCurrentVersion());
        Image icon = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream(logoPath)));
        primaryStage.getIcons().add(icon);

        Rectangle clip = new Rectangle();
        clip.setWidth(1280);
        clip.setHeight(720);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        root.setClip(clip);

        Rectangle2D bounds = currentScreen.getVisualBounds();
        primaryStage.setOnShown(event -> {
            double stageWidth = primaryStage.getWidth();
            double stageHeight = primaryStage.getHeight();

            double centerX = bounds.getMinX() + (bounds.getWidth() - stageWidth) / 2;
            double centerY = bounds.getMinY() + (bounds.getHeight() - stageHeight) / 2;

            primaryStage.setX(centerX);
            primaryStage.setY(centerY);
        });

        root.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {

            primaryStage.setX(event.getScreenX() - x);
            primaryStage.setY(event.getScreenY() - y);

        });
        primaryStage.show();
    }

    public static void loadFXMLIntoPane(Pane currentPagePane, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Launcher.class.getResource(fxmlPath)));
            Pane loadedPane = loader.load();

            currentPagePane.getChildren().clear();
            currentPagePane.getChildren().add(loadedPane);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Screen getScreenOfNode(Node node) {
        Window window = node.getScene().getWindow();

        double windowX = window.getX();
        double windowY = window.getY();

        for (Screen screen : Screen.getScreens()) {
            Rectangle2D bounds = screen.getVisualBounds();
            if (bounds.contains(windowX, windowY)) {
                return screen;
            }
        }

        return Screen.getPrimary();
    }
}
