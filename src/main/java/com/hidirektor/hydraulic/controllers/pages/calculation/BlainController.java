package com.hidirektor.hydraulic.controllers.pages.calculation;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.controllers.notification.NotificationController;
import com.hidirektor.hydraulic.utils.Notification.NotificationUtil;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class BlainController implements Initializable {

    @FXML
    public Label blainCalculationTitle;

    @FXML
    public AnchorPane orderSection, unitInfoSection, unitInfoSectionContainer, orderSectionContainer;

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
    private String secilenPompa = null;
    private String secilenValfTipi = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            addHoverEffectToButtons(clearButton);
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, true, false);
            // Tüm dropdown'ları başlangıçta disabled yap
            disableAllDropdowns();
            comboBoxListener();
        });
    }
    
    private void disableAllDropdowns() {
        motorComboBox.setDisable(true);
        sogutmaComboBox.setDisable(true);
        tablaKilitComboBox.setDisable(true);
        pompaComboBox.setDisable(true);
        valfTipiComboBox.setDisable(true);
        yagTankiComboBox.setDisable(true);
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
            
            // Ünite bilgileri açıldığında dropdown'ları disabled bırak (sıralı aktif olacaklar)
            if(!wasExpanded && isUnitInfoSectionExpanded) {
                disableAllDropdowns();
            }
        } else if(actionEvent.getSource().equals(clearButton)) {
            // Temizle butonu - şimdilik boş, sonra implement edilecek
        }
    }
    
    @FXML
    public void handleOrderSectionClick(MouseEvent event) {
        // Sadece ana AnchorPane'e tıklandığında (buton veya içerik dışında) collapse/expand yap
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return; // Buton veya ImageView'e tıklandıysa işlem yapma
        }
        // Ana AnchorPane'e tıklandığında collapse/expand yap
        collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false, false);
        isOrderSectionExpanded = !isOrderSectionExpanded;
    }
    
    @FXML
    public void handleSectionClick(MouseEvent event) {
        // Sadece ana AnchorPane'e tıklandığında (buton veya içerik dışında) collapse/expand yap
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return; // Buton veya ImageView'e tıklandıysa işlem yapma
        }
        // Ana AnchorPane'e tıklandığında collapse/expand yap
        boolean wasExpanded = isUnitInfoSectionExpanded;
        collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, false);
        isUnitInfoSectionExpanded = !isUnitInfoSectionExpanded;
        
        // Ünite bilgileri açıldığında dropdown'ları disabled bırak (sıralı aktif olacaklar)
        if(!wasExpanded && isUnitInfoSectionExpanded) {
            disableAllDropdowns();
        }
    }
    
    @FXML
    public void stopEventPropagation(MouseEvent event) {
        // İçerideki AnchorPane'e tıklandığında event propagation'ı durdur
        event.consume();
    }
    
    @FXML
    public void handleInviteUserClick(MouseEvent event) {
        NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), 
            NotificationController.NotificationType.WARNING, 
            "Ana sunucuya bağlanılamadı lütfen geliştirici ile iletişime geçin.", 
            "hidirektor@gmail.com");
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
            }
            // Motor dropdown'ını aktif et
            if(motorComboBox.isDisable()) {
                motorComboBox.setDisable(false);
                motorComboBox.getItems().clear();
                if(SystemDefaults.getLocalHydraulicData().blainMotorMap.containsKey("0")) {
                    motorComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().blainMotorMap.get("0"));
                }
            }
        });
        
        UIProcess.changeInputDataForComboBox(motorComboBox, newValue -> {
            // Motor seçildiğinde Sipariş Bilgileri bölümünü kapat
            if(isOrderSectionExpanded) {
                collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false, true);
                isOrderSectionExpanded = false;
            }
            // Motor seçildiğinde Soğutma dropdown'ını aktif et
            if(sogutmaComboBox.isDisable()) {
                sogutmaComboBox.setDisable(false);
                sogutmaComboBox.getItems().clear();
                sogutmaComboBox.getItems().addAll("Var", "Yok");
            }
        }, null);
        
        UIProcess.changeInputDataForComboBox(sogutmaComboBox, newValue -> {
            secilenSogutma = newValue.toString();
            // Soğutma seçildiğinde Tabla Kilit dropdown'ını aktif et
            if(tablaKilitComboBox.isDisable()) {
                tablaKilitComboBox.setDisable(false);
                tablaKilitComboBox.getItems().clear();
                tablaKilitComboBox.getItems().addAll("Var", "Yok");
            }
            // Pompa seçilmişse valf tipini güncelle
            if(secilenPompa != null) {
                updateValfTipiOptions();
            }
        }, null);
        
        UIProcess.changeInputDataForComboBox(tablaKilitComboBox, newValue -> {
            secilenTablaKilit = newValue.toString();
            // Tabla Kilit seçildiğinde Pompa dropdown'ını aktif et
            if(pompaComboBox.isDisable()) {
                pompaComboBox.setDisable(false);
                pompaComboBox.getItems().clear();
                if(SystemDefaults.getLocalHydraulicData().blainPompaMap.containsKey("0")) {
                    pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().blainPompaMap.get("0"));
                }
            }
        }, null);
        
        UIProcess.changeInputDataForComboBox(pompaComboBox, newValue -> {
            secilenPompa = newValue.toString();
            // Pompa seçildiğinde Valf Tipi dropdown'ını aktif et ve güncelle
            if(valfTipiComboBox.isDisable()) {
                valfTipiComboBox.setDisable(false);
            }
            updateValfTipiOptions();
        }, null);
        
        UIProcess.changeInputDataForComboBox(valfTipiComboBox, newValue -> {
            secilenValfTipi = newValue.toString();
            // Valf tipi seçildiğinde Yağ Tankı dropdown'ını aktif et ve güncelle
            if(yagTankiComboBox.isDisable()) {
                yagTankiComboBox.setDisable(false);
            }
            updateYagTankiOptions();
        }, null);
    }
    
    private void updateValfTipiOptions() {
        if(valfTipiComboBox == null) return;
        
        valfTipiComboBox.getItems().clear();
        
        // Soğutma kriterine göre seçenekleri belirle
        java.util.List<String> valfTipiOptions;
        if(secilenSogutma != null && secilenSogutma.equals("Var")) {
            // Soğutma Var ise KV1S hariç
            valfTipiOptions = java.util.Arrays.asList("KV2S", "EV100 3/4\"", "EV100 1\"1/2");
        } else {
            // Soğutma Yok ise veya henüz seçilmemişse hepsi
            valfTipiOptions = java.util.Arrays.asList("KV1S", "KV2S", "EV100 3/4\"", "EV100 1\"1/2");
        }
        
        valfTipiComboBox.getItems().addAll(valfTipiOptions);
        
        // Eğer seçili değer artık listede yoksa, seçimi temizle
        String currentValue = valfTipiComboBox.getValue();
        if(currentValue != null && !valfTipiComboBox.getItems().contains(currentValue)) {
            valfTipiComboBox.getSelectionModel().clearSelection();
            secilenValfTipi = null;
            // Valf tipi temizlendiğinde yağ tankı seçeneklerini de güncelle
            updateYagTankiOptions();
        }
    }
    
    private void updateYagTankiOptions() {
        if(yagTankiComboBox == null) return;
        
        yagTankiComboBox.getItems().clear();
        
        // Valf tipi seçilmemişse tüm seçenekleri göster
        if(secilenValfTipi == null) {
            yagTankiComboBox.getItems().addAll("BTH 75", "BTH 150", "BTH 250", "BTH 400", "BTH 600", "BTH 1000");
            // Eğer seçili değer varsa ve listede yoksa, seçimi temizle
            String currentValue = yagTankiComboBox.getValue();
            if(currentValue != null && !yagTankiComboBox.getItems().contains(currentValue)) {
                yagTankiComboBox.getSelectionModel().clearSelection();
            }
            return;
        }
        
        // Valf tipine göre yağ tankı seçeneklerini belirle
        java.util.List<String> yagTankiOptions;
        switch(secilenValfTipi) {
            case "KV1S":
                // KV1S → sadece BTH 75
                yagTankiOptions = java.util.Arrays.asList("BTH 75");
                break;
            case "KV2S":
                // KV2S → BTH 75, BTH 150
                yagTankiOptions = java.util.Arrays.asList("BTH 75", "BTH 150");
                break;
            case "EV100 3/4\"":
                // EV100 3/4 → BTH 150, BTH 250, BTH 400
                yagTankiOptions = java.util.Arrays.asList("BTH 150", "BTH 250", "BTH 400");
                break;
            case "EV100 1\"1/2":
                // EV100 1"1/2 → BTH 250, BTH 400, BTH 600
                yagTankiOptions = java.util.Arrays.asList("BTH 250", "BTH 400", "BTH 600");
                break;
            default:
                // Varsayılan olarak tüm seçenekleri göster
                yagTankiOptions = java.util.Arrays.asList("BTH 75", "BTH 150", "BTH 250", "BTH 400", "BTH 600", "BTH 1000");
                break;
        }
        
        yagTankiComboBox.getItems().addAll(yagTankiOptions);
        
        // Eğer seçili değer artık listede yoksa, seçimi temizle
        String currentValue = yagTankiComboBox.getValue();
        if(currentValue != null && !yagTankiComboBox.getItems().contains(currentValue)) {
            yagTankiComboBox.getSelectionModel().clearSelection();
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

