package com.hidirektor.hydraulic.controllers;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.controllers.notification.NotificationController;
import com.hidirektor.hydraulic.utils.Notification.NotificationUtil;
import com.hidirektor.hydraulic.utils.SceneUtil;
import com.hidirektor.hydraulic.utils.Utils;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class LandingController implements Initializable {

    @FXML
    public AnchorPane hamburgerMenu;

    @FXML
    public HBox programControlBar;

    @FXML
    public ImageView mainLogo;

    @FXML
    public ImageView closeIcon, minimizeIcon, expandIcon;

    @FXML
    public StackPane currentPagePane;

    @FXML
    public Button homeButton, hydraulicUnitsButton, ticketButton, usersButton, debugButton, licenseButton, sourceUsageButton, schemeButton, settingsButton;

    @FXML
    public Button createClassicUnit, createPowerPackUnit;

    @FXML
    public ImageView contactUsButton;

    @FXML
    public Label sectionNameLabel;

    private Stage currentStage = null;
    public static boolean isDebugOpened = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            currentStage = (Stage) sectionNameLabel.getScene().getWindow();

            addHoverEffect(closeIcon, minimizeIcon, expandIcon, contactUsButton);
            addHoverEffectToButtons(createClassicUnit, createPowerPackUnit);

            homeButton.fire();
            Utils.clickButton(homeButton, 1);

            programControlBar.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    if(!currentStage.isMaximized()) {
                        expandProgram();
                    }
                }
            });

            sectionNameLabel.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    if(currentStage.isMaximized()) {
                        expandProgram();
                    }
                }
            });
        });
    }

    @FXML
    public void closeProgram() {
        Stage stage = (Stage) (sectionNameLabel.getScene().getWindow());
        stage.close();
    }

    @FXML
    public void minimizeProgram() {
        Stage stage = (Stage) (sectionNameLabel.getScene().getWindow());
        boolean isMaximized = stage.isMaximized();

        if(isMaximized) {
            stage.setMaximized(false);
            applyClipToRoot();
        }

        stage.setIconified(true);
    }

    @FXML
    public void expandProgram() {
        boolean isMaximized = currentStage.isMaximized();

        Node root = sectionNameLabel.getScene().getRoot();

        if (isMaximized) {
            applyClipToRoot();
        } else {
            root.setClip(null);
        }

        currentStage.setMaximized(!isMaximized);
    }

    @FXML
    public void handleClick(ActionEvent actionEvent) {
        if(actionEvent.getSource().equals(homeButton)) {
            SceneUtil.loadFXMLIntoPane(currentPagePane, "fxml/Dashboard.fxml");
        } else if(actionEvent.getSource().equals(hydraulicUnitsButton)) {
            //Hidrolik Üniteleri
        } else if(actionEvent.getSource().equals(ticketButton)) {
            //Destek Talepleri
        } else if(actionEvent.getSource().equals(usersButton)) {
            //Kullanıcılar
        } else if(actionEvent.getSource().equals(debugButton)) {
            //Debug Modu
            /*if(!isDebugOpened) {
                isDebugOpened = true;
            }*/
            Utils.showPopup(SceneUtil.getScreenOfNode(sectionNameLabel), "fxml/DebugMode.fxml", "Hydraulic Tool || Konsol", Modality.NONE, null);
        } else if(actionEvent.getSource().equals(licenseButton)) {
            //Lisans Yönetimi
        } else if(actionEvent.getSource().equals(sourceUsageButton)) {
            //Kaynak Kullanımı
        } else if(actionEvent.getSource().equals(schemeButton)) {
            //2 Boyutlu Şema Alt Programı
        } else if(actionEvent.getSource().equals(settingsButton)) {
            //Ayarlar
            SceneUtil.loadFXMLIntoPane(currentPagePane, "fxml/Settings.fxml");
        } else if(actionEvent.getSource().equals(createClassicUnit)) {
            //Klasik Ünite Oluştur
            SceneUtil.loadFXMLIntoPane(currentPagePane, "fxml/ClassicCalculation.fxml");
        } else if(actionEvent.getSource().equals(createPowerPackUnit)) {
            //PowerPack Ünite Oluştur
            SceneUtil.loadFXMLIntoPane(currentPagePane, "fxml/PowerPackCalculation.fxml");
        } else if(actionEvent.getSource().equals(contactUsButton)) {
            //Destek Butonu
        } else {
            NotificationUtil.showNotification(sectionNameLabel.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Buton Hatası", "Buton hatası meydana geldi. Lütfen yaptığınız işlemle birlikte hatayı bize bildirin.");
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

    private void addHoverEffectToButtons(Button... buttons) {
        ColorAdjust darkenEffect = new ColorAdjust();
        darkenEffect.setBrightness(-0.5);

        for (Button button : buttons) {
            button.setOnMouseEntered(event -> button.setEffect(darkenEffect));
            button.setOnMouseExited(event -> button.setEffect(null));
        }
    }

    private void applyClipToRoot() {
        Node root = sectionNameLabel.getScene().getRoot();
        Rectangle clip = new Rectangle();
        clip.setWidth(1280);
        clip.setHeight(720);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        root.setClip(clip);
    }
}