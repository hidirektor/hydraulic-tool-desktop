package com.hidirektor.dashboard.controllers.notification;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class NotificationController {

    @FXML
    private AnchorPane notificationPane;

    @FXML
    private ImageView notificationIcon;

    @FXML
    private Label notificationTitle;

    @FXML
    private Label notificationDescription;

    @FXML
    private ImageView closeNotification;

    public void setNotification(String title, String description, NotificationType type) {
        notificationTitle.setText(title);
        notificationDescription.setText(description);

        switch (type) {
            case SUCCESS:
                notificationIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/icons/icon_notification_success.png"))));
                notificationPane.setStyle("-fx-background-color: #D9FBEE; -fx-border-color: #C0F8E3; -fx-border-width: 3;");
                break;
            case WARNING:
                notificationIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/icons/icon_notification_warning.png"))));
                notificationPane.setStyle("-fx-background-color: #FBF1D9; -fx-border-color: #FBCC00; -fx-border-width: 3;");
                break;
            case INFO:
                notificationIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/icons/icon_notification_info.png"))));
                notificationPane.setStyle("-fx-background-color: #D9E7FB; -fx-border-color: #C0D7F8; -fx-border-width: 3;");
                break;
            case ALERT:
                notificationIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/icons/icon_notification_alert.png"))));
                notificationPane.setStyle("-fx-background-color: #FBDAD9; -fx-border-color: #F8C1C0; -fx-border-width: 3;");
                break;
        }

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), notificationPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), notificationPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(3));

        fadeOut.setOnFinished(e -> closeNotificationStage());
        fadeOut.play();

        closeNotification.setOnMouseClicked(e -> closeNotificationStage());
    }

    private void closeNotificationStage() {
        Stage currentStage = (Stage) notificationIcon.getScene().getWindow();
        if (currentStage != null) {
            currentStage.close();
        }
    }

    public enum NotificationType {
        SUCCESS, WARNING, INFO, ALERT
    }
}
