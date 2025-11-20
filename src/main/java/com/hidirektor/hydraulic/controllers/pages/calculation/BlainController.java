package com.hidirektor.hydraulic.controllers.pages.calculation;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.controllers.notification.NotificationController;
import com.hidirektor.hydraulic.utils.Model.Table.PartListTable;
import com.hidirektor.hydraulic.utils.Notification.NotificationUtil;
import com.hidirektor.hydraulic.utils.File.PDF.PDFUtil;
import com.hidirektor.hydraulic.utils.Process.UIProcess;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.Yaml;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class BlainController implements Initializable {

    @FXML
    public Label blainCalculationTitle;

    @FXML
    public AnchorPane orderSection, unitInfoSection, calculationResultSection, partListSection, unitSchemeSection, unitInfoSectionContainer, orderSectionContainer, calculationResultSectionContainer, partListSectionContainer, unitSchemeSectionContainer;

    @FXML
    public Button orderSectionButton, unitInfoSectionButton, calculationResultSectionButton, partListSectionButton, unitSchemeSectionButton, clearButton, openPDFInExplorerButton;

    @FXML
    public ImageView orderSectionButtonImage, unitInfoSectionButtonImage, calculationResultSectionButtonImage, partListSectionButtonImage, unitSchemeSectionButtonImage, clearButtonImage;
    
    /*
    Parça listesi componentleri
     */
    @FXML
    private TableView<PartListTable> partListTable;

    @FXML
    private TableColumn<PartListTable, String> malzemeKodu;

    @FXML
    private TableColumn<PartListTable, String> secilenMalzeme;

    @FXML
    private TableColumn<PartListTable, String> adet;

    @FXML
    public ImageView resultImage;

    @FXML
    public Label resultImageTitle;

    @FXML
    public TextArea resultTextArea;

    @FXML
    public TextField siparisNumarasiField, inviteUserTextField;

    @FXML
    public ComboBox<String> motorComboBox, sogutmaComboBox, tablaKilitComboBox, 
                            pompaComboBox, valfTipiComboBox, yagTankiComboBox, silindirSayisiCombo;

    /*
    Ünite Şeması Componentleri
     */
    @FXML
    public ImageView schemePageOne, schemePageTwo;
    
    @FXML
    public javafx.scene.layout.StackPane schemePageOneContainer, schemePageTwoContainer;
    
    @FXML
    public AnchorPane schemePageOneOverlay, schemePageTwoOverlay;
    
    @FXML
    public javafx.scene.Group schemePageOneIconGroup, schemePageTwoIconGroup;

    boolean isOrderSectionExpanded = false, isUnitInfoSectionExpanded = false, isCalculationResultSectionExpanded = false, isPartListSectionExpanded = false, isUnitSchemeSectionExpanded = false;
    
    private String silindirSayisi = null;
    
    public static String girilenSiparisNumarasi = "";
    
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
            collapseAndExpandSection(partListSection, isPartListSectionExpanded, partListSectionButtonImage, false, true);
            collapseAndExpandSection(unitSchemeSection, isUnitSchemeSectionExpanded, unitSchemeSectionButtonImage, false, true);
            // Tüm dropdown'ları başlangıçta disabled yap
            disableAllDropdowns();
            comboBoxListener();
            // TextArea scrollbar'ını gizle
            hideTextAreaScrollbars();
            // Parça listesi tablosunu başlat
            initializePartListTable();
        });
    }
    
    private void hideTextAreaScrollbars() {
        if(resultTextArea != null) {
            // Scrollbar'ları CSS ile gizle, ayrıca programatik olarak da gizle
            resultTextArea.lookupAll(".scroll-bar").forEach(node -> {
                node.setVisible(false);
                node.setManaged(false);
            });
        }
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
        } else if(actionEvent.getSource().equals(partListSectionButton)) {
            if(secilenYagTanki == null || secilenValfTipi == null || secilenTablaKilit == null) {
                NotificationUtil.showNotification(partListSectionButton.getScene().getWindow(), 
                    NotificationController.NotificationType.ALERT, 
                    "Şema Hatası", 
                    "Parça listesini görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
                return;
            }
            collapseAndExpandSection(partListSection, isPartListSectionExpanded, partListSectionButtonImage, false, false);
            isPartListSectionExpanded = !isPartListSectionExpanded;
        } else if(actionEvent.getSource().equals(unitSchemeSectionButton)) {
            collapseAndExpandSection(unitSchemeSection, isUnitSchemeSectionExpanded, unitSchemeSectionButtonImage, false, false);
            isUnitSchemeSectionExpanded = !isUnitSchemeSectionExpanded;
        } else if(actionEvent.getSource().equals(openPDFInExplorerButton)) {
            // PDF dosyasını dosya gezgininde aç
            if(girilenSiparisNumarasi != null && !girilenSiparisNumarasi.trim().isEmpty()) {
                String pdfPath = SystemDefaults.userDataPDFFolderPath + girilenSiparisNumarasi + ".pdf";
                try {
                    java.awt.Desktop.getDesktop().open(new java.io.File(pdfPath));
                } catch (Exception e) {
                    NotificationUtil.showNotification(openPDFInExplorerButton.getScene().getWindow(), 
                        NotificationController.NotificationType.ALERT, 
                        "Dosya Hatası", 
                        "PDF dosyası açılamadı: " + e.getMessage());
                }
            }
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
    public void handlePartListSectionClick(MouseEvent event) {
        // Sadece ana AnchorPane'e tıklandığında (buton veya içerik dışında) collapse/expand yap
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return; // Buton veya ImageView'e tıklandıysa işlem yapma
        }
        // Ana AnchorPane'e tıklandığında collapse/expand yap
        if(secilenYagTanki == null || secilenValfTipi == null || secilenTablaKilit == null) {
            NotificationUtil.showNotification(partListSectionContainer.getScene().getWindow(), 
                NotificationController.NotificationType.ALERT, 
                "Şema Hatası", 
                "Parça listesini görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
            return;
        }
        collapseAndExpandSection(partListSection, isPartListSectionExpanded, partListSectionButtonImage, false, false);
        isPartListSectionExpanded = !isPartListSectionExpanded;
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
            // Sipariş numarasını kaydet
            if(newValue != null && !newValue.trim().isEmpty()) {
                girilenSiparisNumarasi = newValue.trim();
            }
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
        
        // Silindir sayısı combo box listener
        UIProcess.changeInputDataForComboBox(silindirSayisiCombo, newValue -> {
            silindirSayisi = newValue;
            exportSchemeProcess();
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
            if(resultTextArea != null) {
                resultTextArea.clear();
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
                // Parça listesini otomatik yükle
                createAndLoadPartListTable();
                
                // Silindir sayısı combo box'ını aktif et
                if(silindirSayisiCombo != null) {
                    silindirSayisiCombo.setDisable(false);
                    silindirSayisiCombo.getItems().clear();
                    silindirSayisiCombo.getItems().addAll("1 Silindir", "2 Silindir", "4 Silindir");
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
        if(resultTextArea == null) return;
        
        StringBuilder text = new StringBuilder();
        
        // Sipariş numarası
        if(siparisNumarasiField != null && siparisNumarasiField.getText() != null && !siparisNumarasiField.getText().trim().isEmpty()) {
            text.append("Sipariş Numarası: ").append(siparisNumarasiField.getText().trim()).append("\n");
        }
        
        // Motor
        if(motorComboBox != null && motorComboBox.getValue() != null) {
            text.append("Motor: ").append(motorComboBox.getValue()).append("\n");
        }
        
        // Soğutma
        if(secilenSogutma != null) {
            text.append("Soğutma: ").append(secilenSogutma).append("\n");
        }
        
        // Tabla Kilit
        if(secilenTablaKilit != null) {
            text.append("Tabla Kilit: ").append(secilenTablaKilit).append("\n");
        }
        
        // Pompa
        if(secilenPompa != null) {
            text.append("Pompa: ").append(secilenPompa).append("\n");
        }
        
        // Valf Tipi
        if(secilenValfTipi != null) {
            text.append("Valf Tipi: ").append(secilenValfTipi).append("\n");
        }
        
        // Yağ Tankı
        if(secilenYagTanki != null) {
            text.append("Yağ Tankı: ").append(secilenYagTanki).append("\n");
        }
        
        resultTextArea.setText(text.toString());
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

    @FXML
    public void handleUnitSchemeSectionClick(MouseEvent event) {
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return;
        }
        // Hesaplama bitti mi kontrol et (tüm seçimler yapıldı mı)
        if(secilenYagTanki != null && secilenValfTipi != null && secilenTablaKilit != null) {
            collapseAndExpandSection(unitSchemeSection, isUnitSchemeSectionExpanded, unitSchemeSectionButtonImage, false, false);
            isUnitSchemeSectionExpanded = !isUnitSchemeSectionExpanded;
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), 
                NotificationController.NotificationType.ALERT, 
                "Şema Hatası", 
                "Ünite şemasını görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
        }
    }
    
    @FXML
    public void handleSchemePageOneEnter(MouseEvent event) {
        if(schemePageOne != null && schemePageOne.isVisible() && schemePageOneOverlay != null) {
            // Overlay'in boyutunu ImageView'a göre ayarla
            schemePageOneOverlay.setPrefWidth(schemePageOne.getFitWidth());
            schemePageOneOverlay.setPrefHeight(schemePageOne.getFitHeight());
            schemePageOneOverlay.setVisible(true);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), schemePageOneOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }
    
    @FXML
    public void handleSchemePageOneExit(MouseEvent event) {
        if(schemePageOneOverlay != null) {
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), schemePageOneOverlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> schemePageOneOverlay.setVisible(false));
            fadeOut.play();
        }
    }
    
    @FXML
    public void handleSchemePageOneClick(MouseEvent event) {
        if(schemePageOne != null && schemePageOne.isVisible() && schemePageOne.getImage() != null) {
            // Tam ekran görüntüleme işlevi buraya eklenecek
            // showFullscreenImages(0);
        }
    }
    
    @FXML
    public void handleSchemePageTwoEnter(MouseEvent event) {
        if(schemePageTwo != null && schemePageTwo.isVisible() && schemePageTwoOverlay != null) {
            // Overlay'in boyutunu ImageView'a göre ayarla
            schemePageTwoOverlay.setPrefWidth(schemePageTwo.getFitWidth());
            schemePageTwoOverlay.setPrefHeight(schemePageTwo.getFitHeight());
            schemePageTwoOverlay.setVisible(true);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), schemePageTwoOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }
    
    @FXML
    public void handleSchemePageTwoExit(MouseEvent event) {
        if(schemePageTwoOverlay != null) {
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), schemePageTwoOverlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> schemePageTwoOverlay.setVisible(false));
            fadeOut.play();
        }
    }
    
    @FXML
    public void handleSchemePageTwoClick(MouseEvent event) {
        if(schemePageTwo != null && schemePageTwo.isVisible() && schemePageTwo.getImage() != null) {
            // Tam ekran görüntüleme işlevi buraya eklenecek
            // showFullscreenImages(1);
        }
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
        if(silindirSayisiCombo != null) {
            silindirSayisiCombo.getSelectionModel().clearSelection();
            silindirSayisiCombo.getItems().clear();
            silindirSayisiCombo.setDisable(true);
        }
        
        // Tüm seçili değerleri null yap
        secilenSogutma = null;
        secilenTablaKilit = null;
        secilenPompa = null;
        secilenValfTipi = null;
        secilenYagTanki = null;
        secilenMotorDiameter = null;
        silindirSayisi = null;
        
        // Şema görsellerini temizle
        if(schemePageOne != null) {
            schemePageOne.setImage(null);
            schemePageOne.setVisible(false);
            schemePageOne.setFitHeight(0);
        }
        if(schemePageTwo != null) {
            schemePageTwo.setImage(null);
            schemePageTwo.setVisible(false);
            schemePageTwo.setFitHeight(0);
        }
        
        // StackPane'leri ve overlay'leri gizle
        if(schemePageOneContainer != null) {
            schemePageOneContainer.setVisible(false);
            schemePageOneContainer.setManaged(false);
        }
        if(schemePageTwoContainer != null) {
            schemePageTwoContainer.setVisible(false);
            schemePageTwoContainer.setManaged(false);
        }
        if(schemePageOneOverlay != null) {
            schemePageOneOverlay.setVisible(false);
            schemePageOneOverlay.setManaged(false);
        }
        if(schemePageTwoOverlay != null) {
            schemePageTwoOverlay.setVisible(false);
            schemePageTwoOverlay.setManaged(false);
        }
        
        // "Dosyada Göster" butonunu gizle
        if(openPDFInExplorerButton != null) {
            openPDFInExplorerButton.setVisible(false);
            openPDFInExplorerButton.setManaged(false);
        }
        
        // Görseli temizle
        if(resultImage != null) {
            resultImage.setImage(null);
        }
        if(resultImageTitle != null) {
            resultImageTitle.setText("Lütfen önce hesaplamayı bitirin.");
        }
        if(resultTextArea != null) {
            resultTextArea.clear();
        }
        
        // Parça listesini temizle
        if(partListTable != null) {
            partListTable.getItems().clear();
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
        if(isPartListSectionExpanded) {
            collapseAndExpandSection(partListSection, isPartListSectionExpanded, partListSectionButtonImage, false, true);
            isPartListSectionExpanded = false;
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
    
    private void initializePartListTable() {
        if(partListTable != null && malzemeKodu != null && secilenMalzeme != null && adet != null) {
            malzemeKodu.setCellValueFactory(new PropertyValueFactory<>("malzemeKoduProperty"));
            secilenMalzeme.setCellValueFactory(new PropertyValueFactory<>("malzemeAdiProperty"));
            adet.setCellValueFactory(new PropertyValueFactory<>("malzemeAdetProperty"));
        }
    }
    
    @FXML
    public void copyToClipboard() {
        if(partListTable == null) return;
        
        StringBuilder clipboardString = new StringBuilder();
        ObservableList<PartListTable> veriler = partListTable.getItems();

        for (PartListTable veri : veriler) {
            clipboardString.append(veri.getMalzemeKoduProperty()).append(" ");
            clipboardString.append(veri.getMalzemeAdiProperty()).append(" ");
            clipboardString.append(veri.getMalzemeAdetProperty()).append("\n");
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    public void exportAsExcel() {
        if(partListTable == null) return;
        
        ObservableList<PartListTable> veriler = partListTable.getItems();
        String excelFileName = SystemDefaults.userDataExcelFolderPath + girilenSiparisNumarasi + ".xlsx";

        Multimap<String, PartListTable> malzemeMultimap = LinkedListMultimap.create();
        Multimap<String, PartListTable> filteredMultimap = LinkedListMultimap.create();
        Multimap<String, PartListTable> duplicateMultimap = LinkedListMultimap.create();

        for (PartListTable rowData : veriler) {
            if (!(rowData.getMalzemeKoduProperty().equals("----") && rowData.getMalzemeAdetProperty().equals("----"))) {
                String malzemeKey = rowData.getMalzemeKoduProperty();

                filteredMultimap.put(malzemeKey, new PartListTable(
                        rowData.getMalzemeKoduProperty(),
                        rowData.getMalzemeAdiProperty(),
                        rowData.getMalzemeAdetProperty()
                ));
            }
        }

        List<String> keysToRemove = new ArrayList<>();
        Iterator<String> iterator = filteredMultimap.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();

            // "000-00-00-000" ve "Veri Yok" olmayan key'ler için işlem yap
            if (!key.equals("000-00-00-000") && !key.equals("Veri Yok")) {
                int toplamAdet = 0;
                String currentAdet = "";
                String malzemeKodu = null;
                String malzemeAdi = null;

                // Aynı key'e sahip öğeleri topla
                for (PartListTable data : filteredMultimap.get(key)) {
                    if (data.getMalzemeAdetProperty() != null && !data.getMalzemeAdetProperty().isEmpty() && !data.getMalzemeAdetProperty().contains("Lt")) {
                        try {
                            toplamAdet += Integer.parseInt(data.getMalzemeAdetProperty());
                        } catch (NumberFormatException e) {
                            // Parse edilemezse currentAdet'e at
                            currentAdet = data.getMalzemeAdetProperty();
                        }
                    } else {
                        currentAdet = data.getMalzemeAdetProperty();
                    }
                    if (malzemeKodu == null) {
                        malzemeKodu = data.getMalzemeKoduProperty();
                    }
                    if (malzemeAdi == null) {
                        malzemeAdi = data.getMalzemeAdiProperty();
                    }
                }

                // duplicateMultimap'e, key ve toplam adet bilgisi ile veri ekle
                PartListTable duplicateData;
                if(toplamAdet > 0) {
                    duplicateData = new PartListTable(malzemeKodu, malzemeAdi, String.valueOf(toplamAdet));
                } else {
                    duplicateData = new PartListTable(malzemeKodu, malzemeAdi, currentAdet);
                }
                duplicateMultimap.put(key, duplicateData);

                // Silinecek anahtarı işaretle
                keysToRemove.add(key);
            }
        }

        for (String key : keysToRemove) {
            filteredMultimap.removeAll(key);
        }

        // filteredMultimap'teki elemanları sırayla ekle
        for (String key : filteredMultimap.keySet()) {
            for (PartListTable data : filteredMultimap.get(key)) {
                malzemeMultimap.put(key, data);
            }
        }

        // duplicateMultimap'teki elemanları sırayla ekle
        for (String key : duplicateMultimap.keySet()) {
            for (PartListTable data : duplicateMultimap.get(key)) {
                malzemeMultimap.put(key, data);
            }
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Malzeme Listesi");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Malzeme Kodu");
            headerRow.createCell(1).setCellValue("Seçilen Malzeme");
            headerRow.createCell(2).setCellValue("Adet");

            int excelRowIndex = 1;
            for (PartListTable rowData : malzemeMultimap.values()) {
                Row row = sheet.createRow(excelRowIndex++);
                row.createCell(0).setCellValue(rowData.getMalzemeKoduProperty());
                row.createCell(1).setCellValue(rowData.getMalzemeAdiProperty());
                row.createCell(2).setCellValue(rowData.getMalzemeAdetProperty());
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFileName)) {
                workbook.write(fileOut);
                System.out.println("Excel dosyası başarıyla oluşturuldu: " + excelFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtil.showNotification(partListSectionButton.getScene().getWindow(), 
                NotificationController.NotificationType.ALERT, 
                "Hata", 
                "Excel dosyası oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }
    
    private void createAndLoadPartListTable() {
        if(partListTable == null) return;
        
        // Parça listesini temizle
        partListTable.getItems().clear();
        
        // Sipariş numarasını kaydet
        if(siparisNumarasiField != null && siparisNumarasiField.getText() != null && !siparisNumarasiField.getText().trim().isEmpty()) {
            girilenSiparisNumarasi = siparisNumarasiField.getText().trim();
        }
        
        // Parça listesini yükle
        // Bu metodlar blain_parts.yml dosyasından verileri okuyacak
        // Şimdilik placeholder metodlar ekleyeceğim
        loadMotorParts();
        loadPompaParts();
        loadDefaultParts();
        loadValfParts();
        loadTankParts();
        loadTablaSogutmaParts();
        loadPompaValfBlokParts();
    }
    
    private void loadMotorParts() {
        if(motorComboBox == null || motorComboBox.getValue() == null) return;
        
        String selectedMotor = motorComboBox.getValue().trim();
        String blainPartsPath = "/assets/data/programDatabase/blain_parts.yml";
        
        try {
            InputStream input = Launcher.class.getResourceAsStream(blainPartsPath);
            if(input == null) return;
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);
            Map<String, Map<String, Object>> motorData = (Map<String, Map<String, Object>>) yamlData.get("motor");
            
            if(motorData != null) {
                // Motor listesinde seçilen motorun index'ini bul
                LinkedList<String> motorList = SystemDefaults.getLocalHydraulicData().blainMotorMap.get("0");
                if(motorList != null) {
                    int motorIndex = motorList.indexOf(selectedMotor);
                    if(motorIndex >= 0) {
                        String motorKey = String.valueOf(motorIndex);
                        Map<String, Object> motorPartsData = motorData.get(motorKey);
                        if(motorPartsData != null) {
                            Map<String, Object> parts = (Map<String, Object>) motorPartsData.get("parts");
                            if(parts != null) {
                                generalLoadFuncBlain(parts, "Motor Parçaları");
                            }
                        }
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadPompaParts() {
        if(pompaComboBox == null || pompaComboBox.getValue() == null) return;
        
        String selectedPompa = pompaComboBox.getValue().trim();
        String blainPartsPath = "/assets/data/programDatabase/blain_parts.yml";
        
        try {
            InputStream input = Launcher.class.getResourceAsStream(blainPartsPath);
            if(input == null) return;
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);
            Map<String, Map<String, Object>> pompaData = (Map<String, Map<String, Object>>) yamlData.get("pompa");
            
            if(pompaData != null) {
                // Pompa listesinde seçilen pompanın index'ini bul
                LinkedList<String> pompaList = SystemDefaults.getLocalHydraulicData().blainPompaMap.get("0");
                if(pompaList != null) {
                    int pompaIndex = pompaList.indexOf(selectedPompa);
                    if(pompaIndex >= 0) {
                        String pompaKey = String.valueOf(pompaIndex);
                        Map<String, Object> pompaPartsData = pompaData.get(pompaKey);
                        if(pompaPartsData != null) {
                            Map<String, Object> parts = (Map<String, Object>) pompaPartsData.get("parts");
                            if(parts != null) {
                                generalLoadFuncBlain(parts, "Pompa Parçaları");
                            }
                        }
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadDefaultParts() {
        // Default parçaları yükle (basınç şalteri hariç)
        String blainPartsPath = "/assets/data/programDatabase/blain_parts.yml";
        
        try {
            InputStream input = Launcher.class.getResourceAsStream(blainPartsPath);
            if(input == null) return;
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);
            Map<String, Map<String, Object>> defaultData = (Map<String, Map<String, Object>>) yamlData.get("default_parts");
            
            if(defaultData != null) {
                Map<String, Object> defaultPartsData = defaultData.get("0");
                if(defaultPartsData != null) {
                    Map<String, Object> parts = (Map<String, Object>) defaultPartsData.get("parts");
                    if(parts != null) {
                        // Basınç şalterini hariç tut
                        Map<String, Object> filteredParts = new HashMap<>();
                        for(Map.Entry<String, Object> entry : parts.entrySet()) {
                            Map<String, String> partDetails = (Map<String, String>) entry.getValue();
                            String malzemeKodu = partDetails.get("malzemeKodu");
                            // Basınç şalteri kodunu kontrol et (150-51-10-454)
                            if(malzemeKodu != null && !malzemeKodu.equals("150-51-10-454")) {
                                filteredParts.put(entry.getKey(), entry.getValue());
                            }
                        }
                        generalLoadFuncBlain(filteredParts, "Standart Parçalar");
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadValfParts() {
        if(valfTipiComboBox == null || valfTipiComboBox.getValue() == null) return;
        
        String selectedValf = valfTipiComboBox.getValue().trim();
        String blainPartsPath = "/assets/data/programDatabase/blain_parts.yml";
        
        try {
            InputStream input = Launcher.class.getResourceAsStream(blainPartsPath);
            if(input == null) return;
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);
            Map<String, Map<String, Object>> valfData = (Map<String, Map<String, Object>>) yamlData.get("valf");
            
            if(valfData != null) {
                // Valf tipine göre index belirle
                String valfKey = null;
                if(selectedValf.equals("KV1S")) {
                    valfKey = "0";
                } else if(selectedValf.equals("KV2S")) {
                    valfKey = "1";
                } else if(selectedValf.equals("EV100 1\"1/2")) {
                    valfKey = "2";
                } else if(selectedValf.equals("EV100 3/4\"")) {
                    valfKey = "3";
                }
                
                if(valfKey != null) {
                    Map<String, Object> valfPartsData = valfData.get(valfKey);
                    if(valfPartsData != null) {
                        Map<String, Object> parts = (Map<String, Object>) valfPartsData.get("parts");
                        if(parts != null) {
                            generalLoadFuncBlain(parts, "Valf Parçaları");
                        }
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadTankParts() {
        if(yagTankiComboBox == null || yagTankiComboBox.getValue() == null) return;
        
        String selectedTank = yagTankiComboBox.getValue().trim();
        String blainPartsPath = "/assets/data/programDatabase/blain_parts.yml";
        
        try {
            InputStream input = Launcher.class.getResourceAsStream(blainPartsPath);
            if(input == null) return;
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);
            Map<String, Map<String, Object>> tankData = (Map<String, Map<String, Object>>) yamlData.get("tank");
            
            if(tankData != null) {
                // Tank tipine göre index belirle
                String tankKey = null;
                if(selectedTank.equals("BTH 75")) {
                    tankKey = "0";
                } else if(selectedTank.equals("BTH 150")) {
                    tankKey = "1";
                } else if(selectedTank.equals("BTH 250")) {
                    tankKey = "2";
                } else if(selectedTank.equals("BTH 400")) {
                    tankKey = "3";
                } else if(selectedTank.equals("BTH 600")) {
                    tankKey = "4";
                } else if(selectedTank.equals("BTH 1000")) {
                    tankKey = "5";
                }
                
                if(tankKey != null) {
                    Map<String, Object> tankPartsData = tankData.get(tankKey);
                    if(tankPartsData != null) {
                        Map<String, Object> parts = (Map<String, Object>) tankPartsData.get("parts");
                        if(parts != null) {
                            generalLoadFuncBlain(parts, "Tank Parçaları");
                        }
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadTablaSogutmaParts() {
        if(secilenTablaKilit == null || secilenSogutma == null) return;
        
        String blainPartsPath = "/assets/data/programDatabase/blain_parts.yml";
        
        try {
            InputStream input = Launcher.class.getResourceAsStream(blainPartsPath);
            if(input == null) return;
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);
            Map<String, Map<String, Object>> tablaSogutmaData = (Map<String, Map<String, Object>>) yamlData.get("tabla-sogutma");
            
            if(tablaSogutmaData != null) {
                // Tabla kilit ve soğutma kombinasyonuna göre index belirle
                String key = null;
                if(secilenTablaKilit.equals("Var") && secilenSogutma.equals("Var")) {
                    key = "0";
                } else if(secilenTablaKilit.equals("Var") && secilenSogutma.equals("Yok")) {
                    key = "1";
                } else if(secilenTablaKilit.equals("Yok") && secilenSogutma.equals("Var")) {
                    key = "2";
                }
                // Tabla kilit Yok ve Soğutma Yok durumu için parça yok
                
                if(key != null) {
                    Map<String, Object> tablaSogutmaPartsData = tablaSogutmaData.get(key);
                    if(tablaSogutmaPartsData != null) {
                        Map<String, Object> parts = (Map<String, Object>) tablaSogutmaPartsData.get("parts");
                        if(parts != null) {
                            generalLoadFuncBlain(parts, "Tabla Kilit ve Soğutma Parçaları");
                        }
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadPompaValfBlokParts() {
        if(secilenPompa == null || secilenValfTipi == null) return;
        
        String blainPartsPath = "/assets/data/programDatabase/blain_parts.yml";
        
        try {
            InputStream input = Launcher.class.getResourceAsStream(blainPartsPath);
            if(input == null) return;
            
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);
            Map<String, Map<String, Object>> pompaValfBlokData = (Map<String, Map<String, Object>>) yamlData.get("pompa-valf-blok");
            
            if(pompaValfBlokData != null) {
                // Pompa ve valf kombinasyonuna göre blok seç
                List<String> keys = determinePompaValfBlokKeys(secilenPompa, secilenValfTipi);
                
                if(keys != null && !keys.isEmpty()) {
                    for(String key : keys) {
                        Map<String, Object> blokPartsData = pompaValfBlokData.get(key);
                        if(blokPartsData != null) {
                            Map<String, Object> parts = (Map<String, Object>) blokPartsData.get("parts");
                            if(parts != null) {
                                generalLoadFuncBlain(parts, "Pompa-Valf Blok Parçaları");
                            }
                        }
                    }
                }
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private List<String> determinePompaValfBlokKeys(String pompa, String valf) {
        List<String> keys = new ArrayList<>();
        
        // Pompa kapasitesini parse et
        double pompaKapasitesi = 0;
        try {
            // Pompa adından kapasiteyi çıkar (örn: "PAVE025#4A 21,7 LT" -> 21.7)
            if(pompa.contains("21,7") || pompa.contains("21.7")) {
                pompaKapasitesi = 21.7;
            } else if(pompa.contains("28")) {
                pompaKapasitesi = 28;
            } else if(pompa.contains("33,4") || pompa.contains("33.4")) {
                pompaKapasitesi = 33.4;
            } else if(pompa.contains("38")) {
                pompaKapasitesi = 38;
            } else if(pompa.contains("45,6") || pompa.contains("45.6")) {
                pompaKapasitesi = 45.6;
            } else if(pompa.contains("53,7") || pompa.contains("53.7")) {
                pompaKapasitesi = 53.7;
            } else if(pompa.contains("71,6") || pompa.contains("71.6")) {
                pompaKapasitesi = 71.6;
            } else if(pompa.contains("98,9") || pompa.contains("98.9")) {
                pompaKapasitesi = 98.9;
            } else if(pompa.contains("121")) {
                pompaKapasitesi = 121;
            } else if(pompa.contains("145")) {
                pompaKapasitesi = 145;
            } else if(pompa.contains("177,9") || pompa.contains("177.9")) {
                pompaKapasitesi = 177.9;
            } else if(pompa.contains("210,5") || pompa.contains("210.5")) {
                pompaKapasitesi = 210.5;
            } else if(pompa.contains("247,4") || pompa.contains("247.4")) {
                pompaKapasitesi = 247.4;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Valf tipine ve pompa kapasitesine göre blok key'lerini belirle
        if(valf.equals("KV1S")) {
            if(pompaKapasitesi > 0 && pompaKapasitesi <= 40) {
                keys.add("0"); // KV1S / 20-40
            } else if(pompaKapasitesi > 40 && pompaKapasitesi <= 80) {
                keys.add("1"); // KV1S / 40-80
            } else if(pompaKapasitesi == 40) {
                keys.add("8"); // KV1S / 40 (Eski model)
            }
        } else if(valf.equals("KV2S")) {
            if(pompaKapasitesi > 0 && pompaKapasitesi <= 40) {
                keys.add("2"); // KV2S / 20-40
            } else if(pompaKapasitesi > 40 && pompaKapasitesi <= 80) {
                keys.add("3"); // KV2S / 40-80
            } else if(pompaKapasitesi > 80) {
                keys.add("4"); // KV2S / 80+
            }
        } else if(valf.equals("EV100 3/4\"")) {
            if(pompaKapasitesi >= 80 && pompaKapasitesi < 100) {
                keys.add("5"); // EV100 3/4 — 80-99 litre
            } else if(pompaKapasitesi >= 100 && pompaKapasitesi < 125) {
                keys.add("6"); // EV100 3/4 — 100-124 litre
            }
        } else if(valf.equals("EV100 1\"1/2")) {
            if(pompaKapasitesi >= 125) {
                // EV100 1.5" için key 7'de 3 blok var (parts içinde '0', '1', '2')
                keys.add("7"); // BLOK 1.5'' EV 100 / 1, 2, 3 / +KS + BG (hepsi key 7'de)
            }
        }
        
        return keys.isEmpty() ? null : keys;
    }
    
    private void generalLoadFuncBlain(Map<String, Object> parts, String separatorText) {
        if(partListTable == null) return;
        
        PartListTable separatorData = new PartListTable("----", separatorText, "----");
        partListTable.getItems().add(separatorData);

        for (Map.Entry<String, Object> entry : parts.entrySet()) {
            Map<String, String> partDetails = (Map<String, String>) entry.getValue();
            
            String malzemeKodu = partDetails.get("malzemeKodu");
            String malzemeAdi = partDetails.get("malzemeAdi");
            String malzemeAdet = partDetails.get("malzemeAdet");

            PartListTable data = new PartListTable(malzemeKodu, malzemeAdi, malzemeAdet);
            partListTable.getItems().add(data);
        }

        partListTable.setRowFactory(tv -> new javafx.scene.control.TableRow<PartListTable>() {
            @Override
            protected void updateItem(PartListTable item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (item.getMalzemeKoduProperty().equals("----") && item.getMalzemeAdetProperty().equals("----")) {
                        setStyle("-fx-background-color: #F9F871; -fx-text-fill: black;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }
    
    /**
     * Blain PDF şema seçim mantığı
     * Seçilen değerlere göre PDF dosya adını ve proje kodunu döndürür
     */
    private String[] getBlainPDFAndProjectCode() {
        if(secilenSogutma == null || secilenTablaKilit == null || secilenValfTipi == null || silindirSayisi == null) {
            return null;
        }
        
        boolean isSogutmaVar = secilenSogutma.equals("Var");
        boolean isTablaKilitVar = secilenTablaKilit.equals("Var");
        
        // Valf tipini normalize et (EV100 3/4" ve EV100 1"1/2" için EV100 olarak kabul et)
        String valfTipiNormalized = secilenValfTipi;
        if(secilenValfTipi.startsWith("EV100")) {
            valfTipiNormalized = "EV100";
        }
        
        // Silindir sayısını parse et
        String cylinderNumberStr = silindirSayisi.replaceAll("[^0-9]", "");
        int cylinderNumber = Integer.parseInt(cylinderNumberStr);
        
        // PDF dosya adı ve proje kodu
        String pdfFileName = null;
        String projectCode = null;
        
        // Soğutma Var, Tabla Kilit Var
        if(isSogutmaVar && isTablaKilitVar) {
            if(valfTipiNormalized.equals("KV2S")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B1.pdf";
                    projectCode = "HS-HP 1231";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B2.pdf";
                    projectCode = "HS-HP 1232";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B3.pdf";
                    projectCode = "HS-HP 1234";
                }
            } else if(valfTipiNormalized.equals("EV100")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B4.pdf";
                    projectCode = "HS-HP 1331";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B5.pdf";
                    projectCode = "HS-HP 1332";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B6.pdf";
                    projectCode = "HS-HP 1334";
                }
            }
        }
        // Soğutma Var, Tabla Kilit Yok
        else if(isSogutmaVar && !isTablaKilitVar) {
            if(valfTipiNormalized.equals("KV2S")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B7.pdf";
                    projectCode = "HS-HP 1241";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B8.pdf";
                    projectCode = "HS-HP 1242";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B9.pdf";
                    projectCode = "HS-HP 1244";
                }
            } else if(valfTipiNormalized.equals("EV100")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B10.pdf";
                    projectCode = "HS-HP 1341";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B11.pdf";
                    projectCode = "HS-HP 1342";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B12.pdf";
                    projectCode = "HS-HP 1344";
                }
            }
        }
        // Soğutma Yok, Tabla Kilit Var
        else if(!isSogutmaVar && isTablaKilitVar) {
            if(valfTipiNormalized.equals("KV1S")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B13.pdf";
                    projectCode = "HS-HP 1121";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B14.pdf";
                    projectCode = "HS-HP 1122";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B15.pdf";
                    projectCode = "HS-HP 1124";
                }
            } else if(valfTipiNormalized.equals("KV2S")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B16.pdf";
                    projectCode = "HS-HP 1221";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B17.pdf";
                    projectCode = "HS-HP 1222";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B18.pdf";
                    projectCode = "HS-HP 1224";
                }
            } else if(valfTipiNormalized.equals("EV100")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B19.pdf";
                    projectCode = "HS-HP 1321";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B20.pdf";
                    projectCode = "HS-HP 1322";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B21.pdf";
                    projectCode = "HS-HP 1324";
                }
            }
        }
        // Soğutma Yok, Tabla Kilit Yok
        else if(!isSogutmaVar && !isTablaKilitVar) {
            if(valfTipiNormalized.equals("KV1S")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B22.pdf";
                    projectCode = "HS-HP 1111";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B23.pdf";
                    projectCode = "HS-HP 1112";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B24.pdf";
                    projectCode = "HS-HP 1114";
                }
            } else if(valfTipiNormalized.equals("KV2S")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B25.pdf";
                    projectCode = "HS-HP 1211";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B26.pdf";
                    projectCode = "HS-HP 1212";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B27.pdf";
                    projectCode = "HS-HP 1214";
                }
            } else if(valfTipiNormalized.equals("EV100")) {
                if(cylinderNumber == 1) {
                    pdfFileName = "B28.pdf";
                    projectCode = "HS-HP 1311";
                } else if(cylinderNumber == 2) {
                    pdfFileName = "B29.pdf";
                    projectCode = "HS-HP 1312";
                } else if(cylinderNumber == 4) {
                    pdfFileName = "B30.pdf";
                    projectCode = "HS-HP 1314";
                }
            }
        }
        
        if(pdfFileName != null && projectCode != null) {
            return new String[]{pdfFileName, projectCode};
        }
        
        return null;
    }
    
    /**
     * PDF şema export işlemi
     */
    public void exportSchemeProcess() {
        // Tüm gerekli seçimler yapılmış mı kontrol et
        if(secilenSogutma == null || secilenTablaKilit == null || secilenValfTipi == null || silindirSayisi == null) {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), 
                NotificationController.NotificationType.ALERT, 
                "Şema Hatası", 
                "Lütfen tüm gerekli seçimleri yapın (Soğutma, Tabla Kilit, Valf Tipi, Silindir Sayısı).");
            return;
        }
        
        // PDF dosya adını ve proje kodunu al
        String[] pdfInfo = getBlainPDFAndProjectCode();
        if(pdfInfo == null) {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), 
                NotificationController.NotificationType.ALERT, 
                "Şema Hatası", 
                "Seçilen kombinasyon için PDF şeması bulunamadı.");
            return;
        }
        
        String pdfFileName = pdfInfo[0];
        String projectCode = pdfInfo[1];
        
        // PDF dosya yolu
        String pdfPath = "/assets/data/hydraulicUnitData/schematicPDF/blain/" + pdfFileName;
        System.out.println("PDF Şema Yolu: " + pdfPath);
        System.out.println("Proje Kodu: " + projectCode);
        
        // PDF'i yükle ve görüntüle
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    // PDF dosyasını resources'dan yükle
                    InputStream pdfStream = Launcher.class.getResourceAsStream(pdfPath);
                    if(pdfStream == null) {
                        throw new IOException("PDF dosyası bulunamadı: " + pdfPath);
                    }
                    
                    // Geçici dosya oluştur
                    java.io.File tempFile = java.io.File.createTempFile("blain_scheme_", ".pdf");
                    tempFile.deleteOnExit();
                    
                    // PDF'i geçici dosyaya kopyala
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = pdfStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    // PDF sayfalarını ImageView'lara yükle
                    PDFUtil.loadPDFPagesToImageViews(tempFile.getAbsolutePath(), schemePageOne, schemePageTwo);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("PDF yüklenirken hata oluştu: " + e.getMessage(), e);
                }
                return null;
            }
        };
        
        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            exception.printStackTrace();
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), 
                NotificationController.NotificationType.ALERT, 
                "PDF Hatası", 
                "PDF dosyası yüklenirken hata oluştu: " + exception.getMessage());
        });
        
        task.setOnSucceeded(event -> {
            // StackPane'leri görünür ve managed yap
            if(schemePageOneContainer != null) {
                schemePageOneContainer.setVisible(true);
                schemePageOneContainer.setManaged(true);
            }
            if(schemePageTwoContainer != null) {
                schemePageTwoContainer.setVisible(true);
                schemePageTwoContainer.setManaged(true);
            }
            
            if(schemePageOne != null) {
                schemePageOne.setVisible(true);
                schemePageOne.setFitHeight(600.0);
            }
            if(schemePageTwo != null) {
                schemePageTwo.setVisible(true);
                schemePageTwo.setFitHeight(600.0);
            }
            
            // Overlay'lerin boyutunu ImageView'lara göre ayarla ve managed yap
            if(schemePageOneOverlay != null && schemePageOne != null) {
                schemePageOneOverlay.setPrefWidth(schemePageOne.getFitWidth());
                schemePageOneOverlay.setPrefHeight(schemePageOne.getFitHeight());
                schemePageOneOverlay.setManaged(true);
            }
            if(schemePageTwoOverlay != null && schemePageTwo != null) {
                schemePageTwoOverlay.setPrefWidth(schemePageTwo.getFitWidth());
                schemePageTwoOverlay.setPrefHeight(schemePageTwo.getFitHeight());
                schemePageTwoOverlay.setManaged(true);
            }
            
            // Icon Group'lara Scale transform ekle
            if(schemePageOneIconGroup != null) {
                javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(3.0, 3.0);
                schemePageOneIconGroup.getTransforms().clear();
                schemePageOneIconGroup.getTransforms().add(scale);
            }
            if(schemePageTwoIconGroup != null) {
                javafx.scene.transform.Scale scale = new javafx.scene.transform.Scale(3.0, 3.0);
                schemePageTwoIconGroup.getTransforms().clear();
                schemePageTwoIconGroup.getTransforms().add(scale);
            }
            
            System.out.println("PDF sayfaları başarıyla yüklendi. Proje Kodu: " + projectCode);
        });
        
        Platform.runLater(() -> {
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        });
    }
}

