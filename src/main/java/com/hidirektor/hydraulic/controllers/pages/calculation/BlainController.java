package com.hidirektor.hydraulic.controllers.pages.calculation;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class BlainController implements Initializable {

    @FXML
    public Label blainCalculationTitle;

    @FXML
    public AnchorPane orderSection, unitInfoSection;

    @FXML
    public Button orderSectionButton, unitInfoSectionButton, clearButton;

    @FXML
    public ImageView orderSectionButtonImage, unitInfoSectionButtonImage, clearButtonImage;

    @FXML
    public TextField siparisNumarasiField, inviteUserTextField;

    @FXML
    public ComboBox<String> motorComboBox, sogutmaComboBox, tablaKilitComboBox, 
                            pompaComboBox, valfTipiComboBox, yagTankiComboBox;

    boolean isOrderSectionExpanded = false, isUnitInfoSectionExpanded = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            addHoverEffectToButtons(clearButton);
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, true, false);
        });
    }

    @FXML
    public void handleClick(ActionEvent actionEvent) {
        if(actionEvent.getSource().equals(orderSectionButton)) {
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false, false);
            isOrderSectionExpanded = !isOrderSectionExpanded;
        } else if(actionEvent.getSource().equals(unitInfoSectionButton)) {
            boolean wasExpanded = isUnitInfoSectionExpanded;
            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, false);
            isUnitInfoSectionExpanded = !isUnitInfoSectionExpanded;
            
            // Ünite bilgileri açıldığında dropdown'ları aktif et ve değerleri ekle
            if(!wasExpanded && isUnitInfoSectionExpanded) {
                // Motor - blain_combo.yml'den yükle
                motorComboBox.setDisable(false);
                motorComboBox.getItems().clear();
                if(SystemDefaults.getLocalHydraulicData().blainMotorMap.containsKey("0")) {
                    motorComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().blainMotorMap.get("0"));
                }
                
                // Soğutma - Var ya da Yok
                sogutmaComboBox.setDisable(false);
                sogutmaComboBox.getItems().clear();
                sogutmaComboBox.getItems().addAll("Var", "Yok");
                
                // Tabla Kilit - Var ya da Yok
                tablaKilitComboBox.setDisable(false);
                tablaKilitComboBox.getItems().clear();
                tablaKilitComboBox.getItems().addAll("Var", "Yok");
                
                // Pompa - blain_combo.yml'den yükle
                pompaComboBox.setDisable(false);
                pompaComboBox.getItems().clear();
                if(SystemDefaults.getLocalHydraulicData().blainPompaMap.containsKey("0")) {
                    pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().blainPompaMap.get("0"));
                }
                
                // Valf Tipi
                valfTipiComboBox.setDisable(false);
                valfTipiComboBox.getItems().clear();
                valfTipiComboBox.getItems().addAll("KV1S", "KV2S", "EV100 3/4\"", "EV100 1\"1/2");
                
                // Yağ Tankı
                yagTankiComboBox.setDisable(false);
                yagTankiComboBox.getItems().clear();
                yagTankiComboBox.getItems().addAll("BTH 75", "BTH 150", "BTH 250", "BTH 400", "BTH 600", "BTH 1000");
            }
        } else if(actionEvent.getSource().equals(clearButton)) {
            // Temizle butonu - şimdilik boş, sonra implement edilecek
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

    private void collapseAndExpandSection(AnchorPane targetPane, boolean isExpanded, ImageView targetImageView, boolean forceToOpen, boolean forceToClose) {
        Image arrowDown = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_arrow_down.png")));
        Image arrowUp = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_arrow_up.png")));

        if(forceToOpen) {
            isExpanded = true;
            targetPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            targetPane.setVisible(true);
            targetImageView.setImage(arrowUp);
        } else {
            if(forceToClose) {
                isExpanded = false;
                targetPane.setPrefHeight(0);
                targetPane.setVisible(false);
                targetImageView.setImage(arrowDown);
            } else {
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
    }
}

