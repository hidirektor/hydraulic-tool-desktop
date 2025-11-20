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
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.LinkedList;
import java.util.Objects;
import java.util.ResourceBundle;

public class BlainController implements Initializable {

    @FXML
    public Label blainCalculationTitle;

    @FXML
    public AnchorPane orderSection, unitInfoSection, calculationResultSection, unitInfoSectionContainer, orderSectionContainer, calculationResultSectionContainer;

    @FXML
    public Button orderSectionButton, unitInfoSectionButton, calculationResultSectionButton, clearButton;

    @FXML
    public ImageView orderSectionButtonImage, unitInfoSectionButtonImage, calculationResultSectionButtonImage, clearButtonImage;

    @FXML
    public ImageView resultImage;

    @FXML
    public Label resultImageTitle;

    @FXML
    public TextField resultTextField;

    @FXML
    public TextField siparisNumarasiField, inviteUserTextField;

    @FXML
    public ComboBox<String> motorComboBox, sogutmaComboBox, tablaKilitComboBox, 
                            pompaComboBox, valfTipiComboBox, yagTankiComboBox;

    boolean isOrderSectionExpanded = false, isUnitInfoSectionExpanded = false, isCalculationResultSectionExpanded = false;
    
    private String secilenSogutma = null;
    private String secilenTablaKilit = null;
    private String secilenPompa = null;
    private String secilenValfTipi = null;
    private String secilenYagTanki = null;
    private Integer secilenMotorDiameter = null; // Seçilen motorun diameter değerini sakla

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            addHoverEffectToButtons(clearButton);
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, true, false);
            collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false, true);
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
        } else if(actionEvent.getSource().equals(calculationResultSectionButton)) {
            collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false, false);
            isCalculationResultSectionExpanded = !isCalculationResultSectionExpanded;
        } else if(actionEvent.getSource().equals(clearButton)) {
            clearAllFields();
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
    public void handleCalculationResultSectionClick(MouseEvent event) {
        // Sadece ana AnchorPane'e tıklandığında (buton veya içerik dışında) collapse/expand yap
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return; // Buton veya ImageView'e tıklandıysa işlem yapma
        }
        // Ana AnchorPane'e tıklandığında collapse/expand yap
        collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false, false);
        isCalculationResultSectionExpanded = !isCalculationResultSectionExpanded;
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
            "Ana sunucuya bağlanılamadı", 
            "Lütfen geliştirici ile iletişime geçin.\nhidirektor@gmail.com");
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
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false, true);
            isOrderSectionExpanded = false;
            
            // Motor seçildiğinde Soğutma dropdown'ını aktif et
            if(sogutmaComboBox.isDisable()) {
                sogutmaComboBox.setDisable(false);
                sogutmaComboBox.getItems().clear();
                sogutmaComboBox.getItems().addAll("Var", "Yok");
            }
            
            // Seçilen motorun diameter değerini al ve sakla
            String selectedMotor = motorComboBox.getValue();
            secilenMotorDiameter = null;
            
            if(selectedMotor != null && !selectedMotor.trim().isEmpty()) {
                selectedMotor = selectedMotor.trim();
                
                // Önce direkt eşleştirme dene
                secilenMotorDiameter = SystemDefaults.getLocalHydraulicData().blainMotorDiameterMap.get(selectedMotor);
                
                // Bulunamazsa, map'teki tüm key'leri kontrol et (trim ile)
                if(secilenMotorDiameter == null) {
                    for(String mapKey : SystemDefaults.getLocalHydraulicData().blainMotorDiameterMap.keySet()) {
                        if(mapKey != null && mapKey.trim().equals(selectedMotor)) {
                            secilenMotorDiameter = SystemDefaults.getLocalHydraulicData().blainMotorDiameterMap.get(mapKey);
                            break;
                        }
                    }
                }
                
                // Hala bulunamazsa, motor listesinden index ile bulmayı dene
                if(secilenMotorDiameter == null && SystemDefaults.getLocalHydraulicData().blainMotorMap.containsKey("0")) {
                    LinkedList<String> motorList = SystemDefaults.getLocalHydraulicData().blainMotorMap.get("0");
                    int motorIndex = motorList.indexOf(selectedMotor);
                    if(motorIndex >= 0) {
                        // Motor listesindeki her motor için map'te ara
                        for(int i = 0; i < motorList.size(); i++) {
                            String motorFromList = motorList.get(i);
                            if(motorFromList != null && motorFromList.trim().equals(selectedMotor)) {
                                Integer diameter = SystemDefaults.getLocalHydraulicData().blainMotorDiameterMap.get(motorFromList.trim());
                                if(diameter != null) {
                                    secilenMotorDiameter = diameter;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            // Motor seçildiğinde pompa seçimini varsa temizle ve pompa listesini güncelle
            if(!pompaComboBox.isDisable()) {
                pompaComboBox.getSelectionModel().clearSelection();
                secilenPompa = null;
                updatePompaOptions();
                // Pompa seçimi temizlendiğinde valf tipini de temizle
                if(valfTipiComboBox.getValue() != null) {
                    valfTipiComboBox.getSelectionModel().clearSelection();
                    secilenValfTipi = null;
                    updateValfTipiOptions();
                }
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
                // Motor seçildiyse diameter'a göre filtrele, değilse tüm pompaları göster
                updatePompaOptions();
            }
            // Tabla kilit değiştiğinde görseli güncelle
            updateResultImage();
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
            // Valf tipi değiştiğinde görseli güncelle
            updateResultImage();
        }, null);
        
        UIProcess.changeInputDataForComboBox(yagTankiComboBox, newValue -> {
            secilenYagTanki = newValue.toString();
            // Yağ tankı seçildiğinde hesaplama sonucu görselini güncelle
            updateResultImage();
        }, null);
    }
    
    private void updatePompaOptions() {
        if(pompaComboBox == null) return;
        
        pompaComboBox.getItems().clear();
        
        // Seçilen motor diameter'ı yoksa veya motor seçilmemişse tüm pompaları göster
        if(secilenMotorDiameter == null) {
            if(SystemDefaults.getLocalHydraulicData().blainPompaMap.containsKey("0")) {
                pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().blainPompaMap.get("0"));
            }
            return;
        }
        
        // Aynı diameter'a sahip pompaları filtrele
        if(SystemDefaults.getLocalHydraulicData().blainPompaMap.containsKey("0")) {
            for(String pompaName : SystemDefaults.getLocalHydraulicData().blainPompaMap.get("0")) {
                if(pompaName == null) continue;
                
                String trimmedPompaName = pompaName.trim();
                Integer pompaDiameter = SystemDefaults.getLocalHydraulicData().blainPompaDiameterMap.get(trimmedPompaName);
                
                // Eğer bulunamazsa, map'teki tüm key'leri kontrol et
                if(pompaDiameter == null) {
                    for(String mapKey : SystemDefaults.getLocalHydraulicData().blainPompaDiameterMap.keySet()) {
                        if(mapKey != null && mapKey.trim().equals(trimmedPompaName)) {
                            pompaDiameter = SystemDefaults.getLocalHydraulicData().blainPompaDiameterMap.get(mapKey);
                            break;
                        }
                    }
                }
                
                // Diameter eşleşiyorsa listeye ekle
                if(pompaDiameter != null && pompaDiameter.equals(secilenMotorDiameter)) {
                    pompaComboBox.getItems().add(pompaName);
                }
            }
        }
        
        // Eğer seçili pompa artık listede yoksa, seçimi temizle
        String currentPompa = pompaComboBox.getValue();
        if(currentPompa != null && !pompaComboBox.getItems().contains(currentPompa)) {
            pompaComboBox.getSelectionModel().clearSelection();
            secilenPompa = null;
            // Pompa seçimi temizlendiğinde valf tipini de temizle
            if(valfTipiComboBox.getValue() != null) {
                valfTipiComboBox.getSelectionModel().clearSelection();
                secilenValfTipi = null;
                updateValfTipiOptions();
            }
        }
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
    
    private void updateResultImage() {
        // Tüm gerekli seçimler yapılmış mı kontrol et
        if(secilenYagTanki == null || secilenValfTipi == null || secilenTablaKilit == null) {
            // Seçimler tamamlanmamışsa görseli temizle
            if(resultImage != null) {
                resultImage.setImage(null);
            }
            if(resultImageTitle != null) {
                resultImageTitle.setText("Lütfen önce hesaplamayı bitirin.");
            }
            if(resultTextField != null) {
                resultTextField.clear();
            }
            return;
        }
        
        // Seçilen değerleri text olarak ekle
        updateResultText();
        
        // Görsel yolunu belirle
        String imagePath = determineImagePath();
        
        if(imagePath != null) {
            try {
                Image originalImage = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream(imagePath)));
                // Beyaz arka planı result section'ın arka plan rengine çevir
                Image processedImage = replaceWhiteBackground(originalImage);
                if(resultImage != null) {
                    resultImage.setImage(processedImage);
                }
                if(resultImageTitle != null) {
                    resultImageTitle.setText(""); // Görsel yüklendiğinde başlık metnini boş bırak
                }
                // Hesaplama sonucu bölümünü aç
                if(!isCalculationResultSectionExpanded) {
                    collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, true, false);
                    isCalculationResultSectionExpanded = true;
                }
                // Ünite bilgileri bölümünü kapat
                if(isUnitInfoSectionExpanded) {
                    collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, true);
                    isUnitInfoSectionExpanded = false;
                }
            } catch (Exception e) {
                System.err.println("Görsel yüklenirken hata oluştu: " + e.getMessage());
                if(resultImageTitle != null) {
                    resultImageTitle.setText("Görsel yüklenemedi: " + imagePath);
                }
            }
        } else {
            // Uygun görsel bulunamadı
            if(resultImage != null) {
                resultImage.setImage(null);
            }
            if(resultImageTitle != null) {
                resultImageTitle.setText("Seçilen kombinasyon için görsel bulunamadı.");
            }
        }
    }
    
    private void updateResultText() {
        if(resultTextField == null) return;
        
        StringBuilder text = new StringBuilder();
        boolean isFirst = true;
        
        // Sipariş numarası
        if(siparisNumarasiField != null && siparisNumarasiField.getText() != null && !siparisNumarasiField.getText().trim().isEmpty()) {
            if(!isFirst) text.append(" | ");
            text.append("Sipariş Numarası: ").append(siparisNumarasiField.getText().trim());
            isFirst = false;
        }
        
        // Motor
        if(motorComboBox != null && motorComboBox.getValue() != null) {
            if(!isFirst) text.append(" | ");
            text.append("Motor: ").append(motorComboBox.getValue());
            isFirst = false;
        }
        
        // Soğutma
        if(secilenSogutma != null) {
            if(!isFirst) text.append(" | ");
            text.append("Soğutma: ").append(secilenSogutma);
            isFirst = false;
        }
        
        // Tabla Kilit
        if(secilenTablaKilit != null) {
            if(!isFirst) text.append(" | ");
            text.append("Tabla Kilit: ").append(secilenTablaKilit);
            isFirst = false;
        }
        
        // Pompa
        if(secilenPompa != null) {
            if(!isFirst) text.append(" | ");
            text.append("Pompa: ").append(secilenPompa);
            isFirst = false;
        }
        
        // Valf Tipi
        if(secilenValfTipi != null) {
            if(!isFirst) text.append(" | ");
            text.append("Valf Tipi: ").append(secilenValfTipi);
            isFirst = false;
        }
        
        // Yağ Tankı
        if(secilenYagTanki != null) {
            if(!isFirst) text.append(" | ");
            text.append("Yağ Tankı: ").append(secilenYagTanki);
            isFirst = false;
        }
        
        resultTextField.setText(text.toString());
    }
    
    private String determineImagePath() {
        if(secilenYagTanki == null || secilenValfTipi == null || secilenTablaKilit == null) {
            return null;
        }
        
        String yagTanki = secilenYagTanki.trim();
        String valfTipi = secilenValfTipi.trim();
        String tablaKilit = secilenTablaKilit.trim();
        
        // BTH 75
        if(yagTanki.equals("BTH 75")) {
            if(valfTipi.equals("KV1S")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth75-kv1s-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth75-kv1s.png";
                }
            } else if(valfTipi.equals("KV2S")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth75-kv2s-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth75-kv2s.png";
                }
            }
        }
        // BTH 150
        else if(yagTanki.equals("BTH 150")) {
            if(valfTipi.equals("KV2S")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth150-kv2s-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth150-kv2s.png";
                }
            } else if(valfTipi.equals("EV100 3/4\"")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth150-ev100_3-4-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth150-ev100_3-4.png";
                }
            }
        }
        // BTH 250
        else if(yagTanki.equals("BTH 250")) {
            if(valfTipi.equals("EV100 3/4\"")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth250-ev100_3-4-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth250-ev100_3-4.png";
                }
            } else if(valfTipi.equals("EV100 1\"1/2")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth250-ev100_1-1-2-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth250-ev100_1-1-2.png";
                }
            }
        }
        // BTH 400
        else if(yagTanki.equals("BTH 400")) {
            if(valfTipi.equals("EV100 3/4\"")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth400-ev100_3-4-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth400-ev100_3-4.png";
                }
            } else if(valfTipi.equals("EV100 1\"1/2")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth400-ev100_1-1-2-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth400-ev100_1-1-2.png";
                }
            }
        }
        // BTH 600
        else if(yagTanki.equals("BTH 600")) {
            if(valfTipi.equals("EV100 3/4\"")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth600-ev100_3-4-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth600-ev100_3-4.png";
                }
            } else if(valfTipi.equals("EV100 1\"1/2")) {
                if(tablaKilit.equals("Var")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth600-ev100_1-1-2-kilit_motor.png";
                } else if(tablaKilit.equals("Yok")) {
                    return "/assets/data/hydraulicUnitData/schematicImages/blain/bth600-ev100_1-1-2.png";
                }
            }
        }
        
        return null;
    }
    
    /**
     * Görseldeki beyaz (#ffffff) pikselleri result section'ın arka plan rengine çevirir.
     * Result section arka plan rengi: accordion-section (rgba(255, 255, 255, 0.34)) + main-component (#E5E7EB)
     * Sonuç renk: yaklaşık #EEEEEE veya #F4F5F7
     */
    private Image replaceWhiteBackground(Image originalImage) {
        if(originalImage == null) return null;
        
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();
        
        if(width <= 0 || height <= 0) return originalImage;
        
        // Result section arka plan rengi (accordion-section yarı saydam beyaz + main-component gri)
        // Hesaplanmış sonuç: rgb(238, 238, 238) veya #EEEEEE
        int backgroundColor = 0xFFEEEEEE; // ARGB formatında: 0xFF = alpha, EEEEEE = renk
        
        WritableImage writableImage = new WritableImage(width, height);
        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        
        // Beyaz rengi tespit etmek için eşik değeri (biraz tolerans için)
        int whiteThreshold = 250; // 250-255 arası beyaz kabul edilir
        
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                
                // ARGB formatından renk bileşenlerini çıkar
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                
                // Beyaz veya beyaza yakın renkleri kontrol et (#ffffff veya yakın tonlar)
                if(red >= whiteThreshold && green >= whiteThreshold && blue >= whiteThreshold) {
                    // Beyaz pikseli result section arka plan rengine çevir
                    pixelWriter.setArgb(x, y, backgroundColor);
                } else {
                    // Diğer pikselleri olduğu gibi bırak
                    pixelWriter.setArgb(x, y, argb);
                }
            }
        }
        
        return writableImage;
    }

    private void clearAllFields() {
        // Tüm text field'ları temizle
        if(siparisNumarasiField != null) {
            siparisNumarasiField.clear();
        }
        if(inviteUserTextField != null) {
            inviteUserTextField.clear();
        }
        
        // Tüm combo box'ları temizle ve disable et
        if(motorComboBox != null) {
            motorComboBox.getSelectionModel().clearSelection();
            motorComboBox.getItems().clear();
            motorComboBox.setDisable(true);
        }
        if(sogutmaComboBox != null) {
            sogutmaComboBox.getSelectionModel().clearSelection();
            sogutmaComboBox.getItems().clear();
            sogutmaComboBox.setDisable(true);
        }
        if(tablaKilitComboBox != null) {
            tablaKilitComboBox.getSelectionModel().clearSelection();
            tablaKilitComboBox.getItems().clear();
            tablaKilitComboBox.setDisable(true);
        }
        if(pompaComboBox != null) {
            pompaComboBox.getSelectionModel().clearSelection();
            pompaComboBox.getItems().clear();
            pompaComboBox.setDisable(true);
        }
        if(valfTipiComboBox != null) {
            valfTipiComboBox.getSelectionModel().clearSelection();
            valfTipiComboBox.getItems().clear();
            valfTipiComboBox.setDisable(true);
        }
        if(yagTankiComboBox != null) {
            yagTankiComboBox.getSelectionModel().clearSelection();
            yagTankiComboBox.getItems().clear();
            yagTankiComboBox.setDisable(true);
        }
        
        // Tüm seçili değerleri null yap
        secilenSogutma = null;
        secilenTablaKilit = null;
        secilenPompa = null;
        secilenValfTipi = null;
        secilenYagTanki = null;
        secilenMotorDiameter = null;
        
        // Görseli temizle
        if(resultImage != null) {
            resultImage.setImage(null);
        }
        if(resultImageTitle != null) {
            resultImageTitle.setText("Lütfen önce hesaplamayı bitirin.");
        }
        if(resultTextField != null) {
            resultTextField.clear();
        }
        
        // Tüm bölümleri collapse et
        if(isUnitInfoSectionExpanded) {
            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, true);
            isUnitInfoSectionExpanded = false;
        }
        if(isCalculationResultSectionExpanded) {
            collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false, true);
            isCalculationResultSectionExpanded = false;
        }
        
        // Sipariş Bilgileri bölümünü expand et
        if(!isOrderSectionExpanded) {
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, true, false);
            isOrderSectionExpanded = true;
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

