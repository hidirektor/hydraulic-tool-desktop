package com.hidirektor.dashboard.utils.Notification;

import com.hidirektor.dashboard.Launcher;
import com.hidirektor.dashboard.controllers.notification.NotificationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class NotificationUtil {

    public static void showNotification(Window owner, NotificationController.NotificationType type, String title, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("fxml/NotificationModal.fxml"));
            AnchorPane notificationPane = loader.load();

            NotificationController controller = loader.getController();
            controller.setNotification(title, description, type);

            Stage notificationStage = new Stage();

            Scene scene = new Scene(notificationPane);
            scene.setFill(null);
            notificationStage.setScene(scene);
            notificationStage.initStyle(StageStyle.TRANSPARENT);

            notificationStage.initOwner(owner);
            notificationStage.setOpacity(0.8);

            Rectangle clip = new Rectangle();
            clip.setWidth(480);
            clip.setHeight(160);
            clip.setArcWidth(16);
            clip.setArcHeight(16);
            notificationPane.setClip(clip);

            notificationStage.setWidth(480);
            notificationStage.setHeight(160);
            notificationStage.setX(owner.getX() + (owner.getWidth() - notificationStage.getWidth()) / 2);
            notificationStage.setY(owner.getY() + owner.getHeight() - notificationStage.getHeight() - 10);

            notificationStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
