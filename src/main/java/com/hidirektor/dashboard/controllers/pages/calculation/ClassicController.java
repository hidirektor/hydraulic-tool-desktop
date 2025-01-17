package com.hidirektor.dashboard.controllers.pages.calculation;

import com.hidirektor.dashboard.Launcher;
import com.hidirektor.dashboard.controllers.notification.NotificationController;
import com.hidirektor.dashboard.utils.Notification.NotificationUtil;
import com.hidirektor.dashboard.utils.Validation.ValidationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ClassicController implements Initializable  {

    @FXML
    public AnchorPane orderSection, unitInfoSection, calculationResultSection;

    @FXML
    public Button orderSectionButton, unitInfoSectionButton, calculationResultSectionButton;

    @FXML
    public ImageView orderSectionButtonImage, unitInfoSectionButtonImage, calculationResultSectionButtonImage;

    //Hesaplama alanları:
    @FXML
    public ComboBox motorComboBox, sogutmaComboBox, hidrolikKilitComboBox, pompaComboBox, kompanzasyonComboBox, valfTipiComboBox, kilitMotorComboBox, kilitPompaComboBox;

    @FXML
    public TextField gerekenYagMiktariField;

    boolean orderSectionExpanded = false, unitInfoSectionExpanded = false, calculationResultSectionExpanded = false;


    /*
    Seçilen Değerler:
     */
    public static String secilenUniteTipi = "Klasik";
    public static String girilenSiparisNumarasi = null;
    public static String secilenMotor = null;
    public static String kompanzasyonDurumu = null;
    public static int secilenKampana = 0;
    public static String secilenPompa = null;
    public static double secilenPompaVal;
    public static int girilenTankKapasitesiMiktari = 0;
    public static String secilenHidrolikKilitDurumu = null;
    public static String secilenValfTipi = null;
    public static String secilenKilitMotor = null;
    public static String secilenKilitPompa = null;
    public static String secilenSogutmaDurumu = null;

    public static boolean hesaplamaBitti = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ValidationUtil.applyValidation(gerekenYagMiktariField, ValidationUtil.ValidationType.NUMERIC);
        });
    }

    @FXML
    public void handleClick(ActionEvent actionEvent) {
        if(actionEvent.getSource().equals(orderSectionButton)) {
            collapseAndExpandSection(orderSection, orderSectionExpanded, orderSectionButtonImage);
            orderSectionExpanded = !orderSectionExpanded;
        } else if(actionEvent.getSource().equals(unitInfoSectionButton)) {
            collapseAndExpandSection(unitInfoSection, unitInfoSectionExpanded, unitInfoSectionButtonImage);
            unitInfoSectionExpanded = !unitInfoSectionExpanded;
        } else if(actionEvent.getSource().equals(calculationResultSectionButton)) {
            collapseAndExpandSection(calculationResultSection, calculationResultSectionExpanded, calculationResultSectionButtonImage);
            calculationResultSectionExpanded = !calculationResultSectionExpanded;
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Buton Hatası", "Buton hatası meydana geldi. Lütfen yaptığınız işlemle birlikte hatayı bize bildirin.");
        }
    }

    private void collapseAndExpandSection(AnchorPane targetPane, boolean isExpanded, ImageView targetImageView) {
        Image arrowDown = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_arrow_down.png")));
        Image arrowUp = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_arrow_up.png")));

        if(isExpanded) {
            targetPane.setPrefHeight(0);
            targetPane.setVisible(false);
            targetImageView.setImage(arrowDown);
        } else {
            targetPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            targetPane.setVisible(true);
            targetImageView.setImage(arrowUp);
        }
    }
}
