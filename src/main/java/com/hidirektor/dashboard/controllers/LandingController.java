package com.hidirektor.dashboard.controllers;

import com.hidirektor.dashboard.Launcher;
import com.hidirektor.dashboard.controllers.notification.NotificationController;
import com.hidirektor.dashboard.utils.Notification.NotificationUtil;
import com.hidirektor.dashboard.utils.SceneUtil;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class LandingController implements Initializable {

    @FXML
    public AnchorPane hamburgerMenu, collapsedPane, expandedPane;

    @FXML
    public Circle profilePhoto;

    @FXML
    public ImageView mainLogo;

    @FXML
    public ImageView closeIcon, minimizeIcon, expandIcon;

    @FXML
    public Pane currentPagePane;

    @FXML
    public Button homeButton, hydraulicUnitsButton, ticketButton, usersButton, debugButton, licenseButton, sourceUsageButton, schemeButton, settingsButton;

    @FXML
    public Button homeButtonMini, hydraulicUnitsButtonMini, ticketButtonMini, usersButtonMini, debugButtonMini, licenseButtonMini, sourceUsageButtonMini, schemeButtonMini, settingsButtonMini;

    @FXML
    public ImageView collapseMenuIcon;

    private boolean isMenuVisible = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image templateProfilePhoto = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/template/profile-photo.jpg")));
        profilePhoto.setFill(new ImagePattern(templateProfilePhoto, 0, 0, 0, 0, false));

        Platform.runLater(() -> {
            addHoverEffect(closeIcon, minimizeIcon, expandIcon);

            MouseEvent mousePressedEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
            homeButton.fireEvent(mousePressedEvent);
        });
    }

    @FXML
    public void closeProgram() {
        Stage stage = (Stage) (profilePhoto.getScene().getWindow());
        stage.close();
    }

    @FXML
    public void minimizeProgram() {
        Stage stage = (Stage) (profilePhoto.getScene().getWindow());
        boolean isMaximized = stage.isMaximized();

        if(isMaximized) {
            stage.setMaximized(false);
            applyClipToRoot();
        }

        stage.setIconified(true);
    }

    @FXML
    public void expandProgram() {
        Stage stage = (Stage) (profilePhoto.getScene().getWindow());
        boolean isMaximized = stage.isMaximized();

        Node root = profilePhoto.getScene().getRoot();

        if (isMaximized) {
            applyClipToRoot();
        } else {
            root.setClip(null);
        }

        stage.setMaximized(!isMaximized);
    }

    @FXML
    private void collapseMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), hamburgerMenu);

        Image collapsedIcon = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_expand_menu.png")));
        Image expandedIcon = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_collapse_menu.png")));

        if (isMenuVisible) {
            hamburgerMenu.setPrefWidth(80.0);
            hamburgerMenu.setMinWidth(hamburgerMenu.getPrefWidth());

            isMenuVisible = false;

            expandedPane.setVisible(false);
            collapsedPane.setVisible(true);

            AnchorPane.setLeftAnchor(collapseMenuIcon, 68.0);
            AnchorPane.setTopAnchor(collapseMenuIcon, 48.0);

            collapseMenuIcon.setImage(collapsedIcon);
        } else {
            hamburgerMenu.setMinWidth(Region.USE_COMPUTED_SIZE);
            hamburgerMenu.setPrefWidth(Region.USE_COMPUTED_SIZE);

            isMenuVisible = true;

            expandedPane.setVisible(true);
            collapsedPane.setVisible(false);

            AnchorPane.setLeftAnchor(collapseMenuIcon, 208.0);
            AnchorPane.setTopAnchor(collapseMenuIcon, 40.0);

            collapseMenuIcon.setImage(expandedIcon);
        }

        transition.play();
    }

    @FXML
    public void handleClick(ActionEvent actionEvent) {
        if(actionEvent.getSource().equals(homeButton) || actionEvent.getSource().equals(homeButtonMini)) {
            SceneUtil.loadFXMLIntoPane(currentPagePane, "fxml/Dashboard.fxml");
        } else if(actionEvent.getSource().equals(hydraulicUnitsButton) || actionEvent.getSource().equals(hydraulicUnitsButtonMini)) {
            //Hidrolik Üniteleri
        } else if(actionEvent.getSource().equals(ticketButton) || actionEvent.getSource().equals(ticketButtonMini)) {
            //Destek Talepleri
        } else if(actionEvent.getSource().equals(usersButton) || actionEvent.getSource().equals(usersButtonMini)) {
            //Kullanıcılar
        } else if(actionEvent.getSource().equals(debugButton) || actionEvent.getSource().equals(debugButtonMini)) {
            //Debug Modu
        } else if(actionEvent.getSource().equals(licenseButton) || actionEvent.getSource().equals(licenseButtonMini)) {
            //Lisans Yönetimi
        } else if(actionEvent.getSource().equals(sourceUsageButton) || actionEvent.getSource().equals(sourceUsageButtonMini)) {
            //Kaynak Kullanımı
        } else if(actionEvent.getSource().equals(schemeButton) || actionEvent.getSource().equals(schemeButtonMini)) {
            //2 Boyutlu Şema Alt Programı
        } else if(actionEvent.getSource().equals(settingsButton) || actionEvent.getSource().equals(settingsButtonMini)) {
            //Ayarlar
        } else {
            NotificationUtil.showNotification(collapseMenuIcon.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Buton Hatası", "Buton hatası meydana geldi. Lütfen yaptığınız işlemle birlikte hatayı bize bildirin.");
        }
    }

    private void addHoverEffect(ImageView... imageViews) {
        ColorAdjust darkenEffect = new ColorAdjust();
        darkenEffect.setBrightness(-0.5);

        for (ImageView imageView : imageViews) {
            imageView.setOnMouseEntered(event -> imageView.setEffect(darkenEffect));
            imageView.setOnMouseExited(event -> imageView.setEffect(null));
        }
    }

    private void applyClipToRoot() {
        Node root = profilePhoto.getScene().getRoot();
        Rectangle clip = new Rectangle();
        clip.setWidth(1280);
        clip.setHeight(720);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        root.setClip(clip);
    }
}