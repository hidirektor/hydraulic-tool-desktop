package com.hidirektor.hydraulic.controllers.pages.calculation;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.utils.Process.UIProcess;
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
    
    private String secilenSogutma = null;
    private String secilenTablaKilit = null;
    private String secilenYagTanki = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            addHoverEffectToButtons(clearButton);
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, true, false);
            comboBoxListener();
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
                
                // Valf Tipi - başlangıçta tüm seçenekler
                valfTipiComboBox.setDisable(false);
                updateValfTipiOptions();
                
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

    private void comboBoxListener() {
        UIProcess.changeInputDataForTextField(siparisNumarasiField, newValue -> {
            // Sipariş numarası girildiğinde Ünite Bilgileri bölümünü otomatik aç
            if(!isUnitInfoSectionExpanded) {
                collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, true, false);
                isUnitInfoSectionExpanded = true;
                
                // Ünite bilgileri açıldığında dropdown'ları aktif et ve değerleri ekle
                motorComboBox.setDisable(false);
                motorComboBox.getItems().clear();
                if(SystemDefaults.getLocalHydraulicData().blainMotorMap.containsKey("0")) {
                    motorComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().blainMotorMap.get("0"));
                }
                
                sogutmaComboBox.setDisable(false);
                sogutmaComboBox.getItems().clear();
                sogutmaComboBox.getItems().addAll("Var", "Yok");
                
                tablaKilitComboBox.setDisable(false);
                tablaKilitComboBox.getItems().clear();
                tablaKilitComboBox.getItems().addAll("Var", "Yok");
                
                pompaComboBox.setDisable(false);
                pompaComboBox.getItems().clear();
                if(SystemDefaults.getLocalHydraulicData().blainPompaMap.containsKey("0")) {
                    pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().blainPompaMap.get("0"));
                }
                
                valfTipiComboBox.setDisable(false);
                updateValfTipiOptions();
                
                yagTankiComboBox.setDisable(false);
                yagTankiComboBox.getItems().clear();
                yagTankiComboBox.getItems().addAll("BTH 75", "BTH 150", "BTH 250", "BTH 400", "BTH 600", "BTH 1000");
            }
        });
        
        UIProcess.changeInputDataForComboBox(sogutmaComboBox, newValue -> {
            secilenSogutma = newValue.toString();
            updateValfTipiOptions();
        }, null);
        
        UIProcess.changeInputDataForComboBox(tablaKilitComboBox, newValue -> {
            secilenTablaKilit = newValue.toString();
            updateValfTipiOptions();
        }, null);
        
        UIProcess.changeInputDataForComboBox(yagTankiComboBox, newValue -> {
            secilenYagTanki = newValue.toString();
            updateValfTipiOptions();
        }, null);
    }
    
    private void updateValfTipiOptions() {
        if(valfTipiComboBox == null) return;
        
        valfTipiComboBox.getItems().clear();
        
        // Önce soğutma kriterine göre seçenekleri belirle
        java.util.List<String> sogutmaBasedOptions;
        if(secilenSogutma != null && secilenSogutma.equals("Var")) {
            // Soğutma Var ise KV1S hariç
            sogutmaBasedOptions = java.util.Arrays.asList("KV2S", "EV100 3/4\"", "EV100 1\"1/2");
        } else {
            // Soğutma Yok ise veya henüz seçilmemişse hepsi
            sogutmaBasedOptions = java.util.Arrays.asList("KV1S", "KV2S", "EV100 3/4\"", "EV100 1\"1/2");
        }
        
        // Yağ tankı seçimine göre kısıtla
        java.util.List<String> yagTankiBasedOptions;
        if(secilenYagTanki != null) {
            switch(secilenYagTanki) {
                case "BTH 75":
                    yagTankiBasedOptions = java.util.Arrays.asList("KV1S", "KV2S");
                    break;
                case "BTH 150":
                    yagTankiBasedOptions = java.util.Arrays.asList("KV2S", "EV100 3/4\"");
                    break;
                case "BTH 250":
                    yagTankiBasedOptions = java.util.Arrays.asList("EV100 3/4\"", "EV100 1\"1/2");
                    break;
                case "BTH 400":
                    yagTankiBasedOptions = java.util.Arrays.asList("EV100 3/4\"", "EV100 1\"1/2");
                    break;
                case "BTH 600":
                    yagTankiBasedOptions = java.util.Arrays.asList("EV100 1\"1/2");
                    break;
                default:
                    yagTankiBasedOptions = sogutmaBasedOptions;
                    break;
            }
        } else {
            yagTankiBasedOptions = sogutmaBasedOptions;
        }
        
        // İki kriterin kesişimini al
        java.util.List<String> finalOptions = new java.util.ArrayList<>();
        for(String option : sogutmaBasedOptions) {
            if(yagTankiBasedOptions.contains(option)) {
                finalOptions.add(option);
            }
        }
        
        valfTipiComboBox.getItems().addAll(finalOptions);
        
        // BTH 600 seçildiğinde otomatik olarak EV100 1"1/2 seç
        if(secilenYagTanki != null && secilenYagTanki.equals("BTH 600")) {
            if(finalOptions.contains("EV100 1\"1/2")) {
                valfTipiComboBox.setValue("EV100 1\"1/2");
            }
        } else {
            // Eğer seçili değer artık listede yoksa, seçimi temizle
            String currentValue = valfTipiComboBox.getValue();
            if(currentValue != null && !valfTipiComboBox.getItems().contains(currentValue)) {
                valfTipiComboBox.getSelectionModel().clearSelection();
            }
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

