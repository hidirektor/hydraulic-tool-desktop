package com.hidirektor.hydraulic.controllers.pages.calculation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.controllers.notification.NotificationController;
import com.hidirektor.hydraulic.utils.File.PDF.PDFUtil;
import com.hidirektor.hydraulic.utils.Model.Hydraulic.Kabin;
import com.hidirektor.hydraulic.utils.Model.Hydraulic.Motor;
import com.hidirektor.hydraulic.utils.Model.Table.DataControlTable;
import com.hidirektor.hydraulic.utils.Model.Table.PartListTable;
import com.hidirektor.hydraulic.utils.Notification.NotificationUtil;
import com.hidirektor.hydraulic.utils.Process.UIProcess;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import com.hidirektor.hydraulic.utils.Utils;
import com.hidirektor.hydraulic.utils.Validation.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import me.t3sl4.util.os.desktop.DesktopUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class PowerPackController implements Initializable  {

    @FXML
    public Label powerPackCaclulationTitle;

    @FXML
    public AnchorPane orderSection, unitInfoSection, calculationResultSection, partListSection, unitSchemeSection, calculationControlSection,
                      orderSectionContainer, unitInfoSectionContainer, calculationResultSectionContainer, partListSectionContainer, 
                      unitSchemeSectionContainer, calculationControlSectionContainer;
    
    @FXML
    public ScrollPane mainScrollPane;

    @FXML
    public Button orderSectionButton, unitInfoSectionButton, calculationResultSectionButton, partListSectionButton, unitSchemeSectionButton, calculationControlSectionButton, openPDFInExplorerButton;

    @FXML
    public ImageView orderSectionButtonImage, unitInfoSectionButtonImage, calculationResultSectionButtonImage, partListSectionButtonImage, unitSchemeSectionButtonImage, calculationControlSectionButtonImage;

    boolean isOrderSectionExpanded = false, isUnitInfoSectionExpanded = false, isCalculationResultSectionExpanded = false, isPartListSectionExpanded = false, isUnitSchemeSectionExpanded = false, isCalculationControlSectionExpanded = false;

    /*
    Sipariş Bilgileri Componentleri
     */
    @FXML
    public TextField siparisNumarasiField, inviteUserTextField;

    /*
    Program parametreleri
     */
    @FXML
    public ComboBox motorVoltajComboBox, uniteTipiComboBox, motorGucuComboBox, pompaComboBox, tankTipiComboBox, tankKapasitesiComboBox, platformTipiComboBox, birinciValfComboBox, inisMetoduComboBox, ikinciValfComboBox;

    @FXML
    public TextField genislikTextField, yukseklikTextField, derinlikTextField;

    @FXML
    public VBox tankKapasitesiVBox, ozelTankVBox;

    @FXML
    public Label birinciValfComboLabel, ikinciValfComboLabel;

    /*
    Seçilen değerler
     */
    public static String secilenUniteTipi = "PowerPack";
    public static String girilenSiparisNumarasi;
    public static String secilenMotorTipi = null;
    public static String secilenMotorGucu = null;
    public static String secilenPompa = null;
    public static String uniteTipiDurumu = null;
    public static String secilenTankTipi = null;
    public static String secilenTankKapasitesi = null;
    public static String secilenOzelTankGenislik = null;
    public static String secilenOzelTankYukseklik = null;
    public static String secilenOzelTankDerinlik = null;
    public static String secilenBirinciValf = null;
    public static String secilenInisTipi = null;
    public static String secilenPlatformTipi = null;
    public static String secilenIkinciValf = null;
    public static String kabinKodu = null;

    public static String atananKabin = null;
    public static boolean hesaplamaBitti = false;

    //Sonuç Kontrol Tablosu
    @FXML
    private TableView<DataControlTable> sonucTablo;

    @FXML
    private TableColumn<DataControlTable, String> dataKeyLine;

    @FXML
    private TableColumn<DataControlTable, String> dataValueLine;

    /*
    Sonuç bölümü Componentleri
     */
    @FXML
    public ImageView tankImage;

    @FXML
    public Label tankTitle, tankOlculeriText;

    /*
    Parça listesi componentleri
     */
    @FXML
    public ComboBox manometreCombo, basincSalteriCombo, elPompasiCombo;

    @FXML
    private TableView<PartListTable> partListTable;

    @FXML
    private TableColumn<PartListTable, String> malzemeKodu;

    @FXML
    private TableColumn<PartListTable, String> secilenMalzeme;

    @FXML
    private TableColumn<PartListTable, String> adet;

    private String manometreDurumu = null;
    private String basincSalteriDurumu = null;
    private String elPompasiDurumu = null;

    /*
    Clear Section
     */
    @FXML
    public Button clearButton;

    @FXML
    public ImageView clearButtonImage;

    /*
    Ünite Şeması Componentleri
     */
    @FXML
    public ComboBox<String> basincSalteriSchemeCombo, silindirSayisiCombo;

    @FXML
    public AnchorPane tankImagePaneSection;

    @FXML
    public ImageView schemePageOne, schemePageTwo;

    private String basincSalteriSchemeDurumu = null;
    private String silindirSayisi = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ValidationUtil.applyValidation(genislikTextField, ValidationUtil.ValidationType.NUMERIC);
            ValidationUtil.applyValidation(yukseklikTextField, ValidationUtil.ValidationType.NUMERIC);
            ValidationUtil.applyValidation(derinlikTextField, ValidationUtil.ValidationType.NUMERIC);
            comboBoxListener();

            dataKeyLine.setCellValueFactory(new PropertyValueFactory<>("programParameter"));
            dataValueLine.setCellValueFactory(new PropertyValueFactory<>("selectedParameterValue"));

            malzemeKodu.setCellValueFactory(new PropertyValueFactory<>("malzemeKoduProperty"));
            secilenMalzeme.setCellValueFactory(new PropertyValueFactory<>("malzemeAdiProperty"));
            adet.setCellValueFactory(new PropertyValueFactory<>("malzemeAdetProperty"));

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
            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, false);
            isUnitInfoSectionExpanded = !isUnitInfoSectionExpanded;
        } else if(actionEvent.getSource().equals(calculationResultSectionButton)) {
            if(hesaplamaBitti) {
                collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false, false);
                isCalculationResultSectionExpanded = !isCalculationResultSectionExpanded;
            } else {
                NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Hesaplama sonucunu görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
            }
        } else if(actionEvent.getSource().equals(partListSectionButton)) {
            if(hesaplamaBitti) {
                collapseAndExpandSection(partListSection, isPartListSectionExpanded, partListSectionButtonImage, false, false);
                isPartListSectionExpanded = !isPartListSectionExpanded;

                manometreCombo.setDisable(false);
                manometreCombo.getItems().clear();
                manometreCombo.getItems().addAll("Var", "Yok");
            } else {
                NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Parça listesini görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
            }
        } else if(actionEvent.getSource().equals(unitSchemeSectionButton)) {
            if(hesaplamaBitti) {
                collapseAndExpandSection(unitSchemeSection, isUnitSchemeSectionExpanded, unitSchemeSectionButtonImage, false, false);
                isUnitSchemeSectionExpanded = !isUnitSchemeSectionExpanded;

                basincSalteriSchemeCombo.setDisable(false);
                basincSalteriSchemeCombo.getItems().clear();
                basincSalteriSchemeCombo.getItems().addAll("Var", "Yok");
            } else {
                NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Ünite şemasını görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
            }
        } else if(actionEvent.getSource().equals(calculationControlSectionButton)) {
            collapseAndExpandSection(calculationControlSection, isCalculationControlSectionExpanded, calculationControlSectionButtonImage, false, false);
            isCalculationControlSectionExpanded = !isCalculationControlSectionExpanded;
        } else if(actionEvent.getSource().equals(openPDFInExplorerButton)) {
            if(hesaplamaBitti && girilenSiparisNumarasi != null && !girilenSiparisNumarasi.isEmpty()) {
                String pdfPath = SystemDefaults.userDataPDFFolderPath + girilenSiparisNumarasi + ".pdf";
                PDFUtil.openFileInExplorer(pdfPath);
            } else {
                NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Dosya Hatası", "PDF dosyası henüz oluşturulmamış.");
            }
        } else if(actionEvent.getSource().equals(clearButton)) {
            if(hesaplamaBitti) {
                clearWholeSelections();
            } else {
                NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Temizleme Hatası", "Temizlik işlemi sadece hesaplama bittikten sonra gerçekleştirilebilir.");
            }
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Buton Hatası", "Buton hatası meydana geldi. Lütfen yaptığınız işlemle birlikte hatayı bize bildirin.");
        }
    }

    @FXML
    public void copyToClipboard() {
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
        ObservableList<PartListTable> veriler = partListTable.getItems();
        String excelFileName = SystemDefaults.userDataExcelFolderPath + PowerPackController.girilenSiparisNumarasi + ".xlsx";

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
                        toplamAdet += Integer.parseInt(data.getMalzemeAdetProperty());
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

                // Konsola duplicate verileri yazdır
                System.out.println("Key: " + key + ", Malzeme Kodu: " + malzemeKodu +
                        ", Malzeme Adı: " + malzemeAdi + ", Toplam Adet: " + toplamAdet);

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

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Ünite Tipi", PowerPackController.secilenUniteTipi);
                jsonObject.put("Sipariş Numarası", PowerPackController.girilenSiparisNumarasi);
                jsonObject.put("Motor Voltaj", PowerPackController.secilenMotorTipi);
                jsonObject.put("Ünite Durumu", PowerPackController.uniteTipiDurumu);
                jsonObject.put("Motor Gücü", PowerPackController.secilenMotorGucu);
                jsonObject.put("Pompa", PowerPackController.secilenPompa);
                jsonObject.put("Tank Tipi", PowerPackController.secilenTankTipi);
                jsonObject.put("Tank Kapasitesi", PowerPackController.secilenTankKapasitesi);
                jsonObject.put("Özel Tank Ölçüleri (GxDxY)", PowerPackController.secilenOzelTankGenislik + "x" + PowerPackController.secilenOzelTankDerinlik + "x" + PowerPackController.secilenOzelTankYukseklik);
                jsonObject.put("Platform Tipi", PowerPackController.secilenPlatformTipi);
                jsonObject.put("1. Valf Tipi", PowerPackController.secilenBirinciValf);
                jsonObject.put("İniş Metodu", PowerPackController.secilenInisTipi);
                jsonObject.put("2. Valf Tipi", PowerPackController.secilenIkinciValf);

                if (SystemDefaults.loggedInUser != null) {
                    Utils.createLocalUnitData(SystemDefaults.userLocalUnitDataFilePath,
                            PowerPackController.girilenSiparisNumarasi,
                            Utils.getCurrentUnixTime(),
                            PowerPackController.secilenUniteTipi,
                            null,
                            excelFileName,
                            "no",
                            SystemDefaults.loggedInUser.getUserID(),
                            jsonObject);
                } else {
                    Utils.createLocalUnitData(SystemDefaults.userLocalUnitDataFilePath,
                            PowerPackController.girilenSiparisNumarasi,
                            Utils.getCurrentUnixTime(),
                            PowerPackController.secilenUniteTipi,
                            null,
                            excelFileName,
                            "yes",
                            System.getProperty("user.name"),
                            jsonObject);
                }

                DesktopUtil.startExternalApplicationAsync(excelFileName);
            }

        } catch (IOException e) {
            System.err.println("Excel dosyası oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    private void clearWholeSelections() {
        collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, true, false);
        collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, true);
        collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false, true);
        collapseAndExpandSection(partListSection, isPartListSectionExpanded, partListSectionButtonImage, false, true);
        collapseAndExpandSection(unitSchemeSection, isUnitSchemeSectionExpanded, unitSchemeSectionButtonImage, false, true);
        collapseAndExpandSection(calculationControlSection, isCalculationControlSectionExpanded, calculationControlSectionButtonImage, false, true);

        siparisNumarasiField.clear();

        clearComboBoxSelection(motorVoltajComboBox);
        secilenMotorTipi = null;

        clearComboBoxSelection(uniteTipiComboBox);
        secilenUniteTipi = null;

        clearComboBoxSelection(motorGucuComboBox);
        secilenMotorGucu = null;

        clearComboBoxSelection(pompaComboBox);
        secilenPompa = null;

        clearComboBoxSelection(tankTipiComboBox);
        secilenTankTipi = null;

        clearComboBoxSelection(tankKapasitesiComboBox);
        secilenTankKapasitesi = null;

        genislikTextField.clear();
        genislikTextField.setPromptText("Genişlik");
        secilenOzelTankGenislik = null;

        yukseklikTextField.clear();
        yukseklikTextField.setPromptText("Yükseklik");
        secilenOzelTankYukseklik = null;

        derinlikTextField.clear();
        derinlikTextField.setPromptText("Derinlik");
        secilenOzelTankDerinlik = null;

        clearComboBoxSelection(platformTipiComboBox);
        secilenPlatformTipi = null;

        clearComboBoxSelection(birinciValfComboBox);
        secilenBirinciValf = null;

        clearComboBoxSelection(inisMetoduComboBox);
        secilenInisTipi = null;

        clearComboBoxSelection(ikinciValfComboBox);
        secilenIkinciValf = null;

        sonucTablo.getItems().clear();
        tankTitle.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        tankOlculeriText.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        tankImage.setImage(null);

        clearComboBoxSelection(manometreCombo);
        manometreDurumu = null;

        clearComboBoxSelection(basincSalteriCombo);
        basincSalteriDurumu = null;

        clearComboBoxSelection(elPompasiCombo);
        elPompasiDurumu = null;

        partListTable.getItems().clear();

        clearComboBoxSelection(basincSalteriSchemeCombo);
        basincSalteriSchemeDurumu = null;

        clearComboBoxSelection(silindirSayisiCombo);
        silindirSayisi = null;

        schemePageOne.setImage(null);
        schemePageOne.setVisible(false);
        schemePageOne.setFitHeight(0);
        schemePageTwo.setImage(null);
        schemePageTwo.setVisible(false);
        schemePageTwo.setFitHeight(0);
        
        // "Dosyada Göster" butonunu gizle
        if(openPDFInExplorerButton != null) {
            openPDFInExplorerButton.setVisible(false);
            openPDFInExplorerButton.setManaged(false);
        }

        hesaplamaBitti = false;
    }

    private void comboBoxListener() {
        UIProcess.changeInputDataForTextField(siparisNumarasiField, newValue -> {
            girilenSiparisNumarasi = newValue;

            initMotorTipi();

            tabloGuncelle();

            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, true, false);
        });

        UIProcess.changeInputDataForComboBox(motorVoltajComboBox, newValue -> {
            secilenMotorTipi = newValue.toString();

            // Motor voltajı seçildiğinde Sipariş Bilgileri bölümünü kapat
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false, true);
            isOrderSectionExpanded = false;

            initUniteTipi();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(uniteTipiComboBox, newValue -> {
            uniteTipiDurumu = newValue.toString();

            initMotorGucu();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(motorGucuComboBox, newValue -> {
            secilenMotorGucu = newValue.toString();

            initPompa();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(pompaComboBox, newValue -> {
            secilenPompa = newValue.toString();

            initTankTipi();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(tankTipiComboBox, newValue -> {
            secilenTankTipi = newValue.toString();

            if(secilenTankTipi.contains("Özel")) {
                ozelTankStatus("Özel");
            } else {
                ozelTankStatus("Normal");
                initTankKapasitesi();
            }

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(tankKapasitesiComboBox, newValue -> {
            secilenTankKapasitesi = newValue.toString();

            initPlatformTipi();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForTextField(genislikTextField, newValue -> {
            secilenOzelTankGenislik = newValue.toString();

            yukseklikTextField.setDisable(false);

            tabloGuncelle();
        });

        UIProcess.changeInputDataForTextField(yukseklikTextField, newValue -> {
            secilenOzelTankYukseklik = newValue.toString();

            derinlikTextField.setDisable(false);

            tabloGuncelle();
        });

        UIProcess.changeInputDataForTextField(derinlikTextField, newValue -> {
            secilenOzelTankDerinlik = newValue.toString();

            initPlatformTipi();

            tabloGuncelle();
        });

        UIProcess.changeInputDataForComboBox(platformTipiComboBox, newValue -> {
            secilenPlatformTipi = newValue.toString();

            if(secilenPlatformTipi.equals("ESP")) {
                inisMetoduComboBox.setDisable(false);
                birinciValfComboBox.setDisable(true);
                ikinciValfComboBox.setDisable(true);
                initInisMetodu();
            } else if(secilenPlatformTipi.equals("Devirmeli + Yürüyüş")) {
                inisMetoduComboBox.setDisable(true);
                birinciValfComboBox.setDisable(true);
                ikinciValfComboBox.setDisable(true);
                hesaplaFunc();
            } else {
                inisMetoduComboBox.setDisable(true);
                birinciValfComboBox.setDisable(false);
                initValfTipi();
            }

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(birinciValfComboBox, newValue -> {
            secilenBirinciValf = newValue.toString();

            initIkinciValf();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(inisMetoduComboBox, newValue -> {
            secilenInisTipi = newValue.toString();

            hesaplaFunc();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(ikinciValfComboBox, newValue -> {
            secilenIkinciValf = newValue.toString();

            hesaplaFunc();

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(manometreCombo, newValue -> {
            manometreDurumu = newValue.toString();

            basincSalteriCombo.setDisable(false);
            basincSalteriCombo.getItems().clear();
            basincSalteriCombo.getItems().addAll("Var", "Yok");
        }, null);

        UIProcess.changeInputDataForComboBox(basincSalteriCombo, newValue -> {
            basincSalteriDurumu = String.valueOf(basincSalteriCombo.getValue());

            elPompasiCombo.setDisable(false);
            elPompasiCombo.getItems().clear();
            elPompasiCombo.getItems().addAll("Var", "Yok");
        }, null);

        UIProcess.changeInputDataForComboBox(elPompasiCombo, newValue -> {
            elPompasiDurumu = String.valueOf(elPompasiCombo.getValue());

            createAndLoadPartListTable();
        }, null);

        UIProcess.changeInputDataForComboBox(basincSalteriSchemeCombo, newValue -> {
            basincSalteriSchemeDurumu = String.valueOf(basincSalteriSchemeCombo.getValue());

            silindirSayisiCombo.setDisable(false);
            silindirSayisiCombo.getItems().clear();
            silindirSayisiCombo.getItems().addAll("1 Silindir", "2 Silindir", "3 Silindir", "4 Silindir");
        }, null);

        UIProcess.changeInputDataForComboBox(silindirSayisiCombo, newValue -> {
            exportSchemeProcess();
        }, null);
    }

    private void ozelTankStatus(String tankType) {
        if(tankType.equals("Özel")) {
            tankKapasitesiVBox.setVisible(false);
            tankKapasitesiVBox.setPrefWidth(0);
            tankKapasitesiVBox.setPrefHeight(0);

            ozelTankVBox.setVisible(true);
            ozelTankVBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
            ozelTankVBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
            genislikTextField.setDisable(false);
        } else {
            //Başlangıç durumu:
            tankKapasitesiVBox.setVisible(true);
            tankKapasitesiComboBox.setDisable(false);
            tankKapasitesiVBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
            tankKapasitesiVBox.setPrefHeight(Region.USE_COMPUTED_SIZE);

            ozelTankVBox.setVisible(false);
            ozelTankVBox.setPrefWidth(0);
            ozelTankVBox.setPrefHeight(0);
        }
    }

    private void tabloGuncelle() {
        sonucTablo.getItems().clear();
        DataControlTable data = new DataControlTable("Sipariş Numarası:", girilenSiparisNumarasi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Motor Voltajı:", secilenMotorTipi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Motor Gücü:", secilenMotorGucu);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Pompa:", secilenPompa);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Ünite Tipi:", uniteTipiDurumu);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Tank Tipi:", secilenTankTipi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Tank Kapasitesi:", secilenTankKapasitesi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Özel Tank Ölçüleri: ", "G: " + secilenOzelTankGenislik + " Y: " + secilenOzelTankYukseklik + " D: " + secilenOzelTankDerinlik);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Platform Tipi:", secilenPlatformTipi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("İniş Metodu:", secilenInisTipi);
        sonucTablo.getItems().add(data);

        if(secilenPlatformTipi != null && secilenPlatformTipi.equals("Özel")) {
            data = new DataControlTable("Valf Sayısı:", secilenBirinciValf);
            sonucTablo.getItems().add(data);

            data = new DataControlTable("Valf Tipi:", secilenIkinciValf);
            sonucTablo.getItems().add(data);
        } else {
            data = new DataControlTable("Birinci Valf:", secilenBirinciValf);
            sonucTablo.getItems().add(data);

            data = new DataControlTable("İkinci Valf:", secilenIkinciValf);
            sonucTablo.getItems().add(data);
        }
    }

    private void initMotorTipi() {
        motorVoltajComboBox.getItems().clear();
        motorVoltajComboBox.setDisable(false);
        motorVoltajComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().motorVoltajMap.get("0"));
    }

    private void initMotorGucu() {
        motorGucuComboBox.getItems().clear();
        motorGucuComboBox.setDisable(false);
        if(uniteTipiDurumu.equals("Hidros")) {
            if(secilenMotorTipi.equals("380 V (AC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("0").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            } else if(secilenMotorTipi.equals("220 V (AC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("2").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            } else if(secilenMotorTipi.equals("24 V (DC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("5").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            } else if(secilenMotorTipi.equals("12 V (DC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("4").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            }
        } else {
            if(secilenMotorTipi.equals("380 V (AC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("1").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            } else if(secilenMotorTipi.equals("220 V (AC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("3").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            } else if(secilenMotorTipi.equals("24 V (DC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("7").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            } else if(secilenMotorTipi.equals("12 V (DC)")) {
                motorGucuComboBox.getItems().addAll(
                        SystemDefaults.getLocalHydraulicData().motorGucuMap.get("6").stream()
                                .map(Motor::getName)
                                .collect(Collectors.toList())
                );
            }
        }
    }

    private void initPompa() {
        pompaComboBox.getItems().clear();
        pompaComboBox.setDisable(false);
        if(uniteTipiDurumu.equals("Hidros")) {
            pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().pompaPowerPackMap.get("0"));
        } else {
            pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().pompaPowerPackMap.get("1"));
        }
    }

    private void initUniteTipi() {
        uniteTipiComboBox.getItems().clear();
        uniteTipiComboBox.setDisable(false);
        uniteTipiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().uniteTipiMap.get("0"));
    }

    private void initTankTipi() {
        tankTipiComboBox.getItems().clear();
        tankTipiComboBox.setDisable(false);
        tankTipiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().tankTipiMap.get("0"));
    }

    private void initTankKapasitesi() {
        tankKapasitesiComboBox.getItems().clear();
        tankKapasitesiComboBox.setDisable(false);
        if(secilenTankTipi.equals("Dikey")) {
            if(uniteTipiDurumu.equals("Hidros")) {
                tankKapasitesiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().tankKapasitesiMap.get("0"));
            } else {
                tankKapasitesiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().tankKapasitesiMap.get("1"));
            }
        } else if(secilenTankTipi.equals("Yatay")) {
            if(uniteTipiDurumu.equals("Hidros")) {
                tankKapasitesiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().tankKapasitesiMap.get("2"));
            } else {
                tankKapasitesiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().tankKapasitesiMap.get("3"));
            }
        }
    }

    private void initPlatformTipi() {
        platformTipiComboBox.getItems().clear();
        platformTipiComboBox.setDisable(false);
        platformTipiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().platformTipiMap.get("0"));
    }

    private void initValfTipi() {
        birinciValfComboBox.getItems().clear();
        birinciValfComboBox.setDisable(false);
        if(secilenPlatformTipi.equals("Özel")) {
            //Platform özelse:
            initCustomPlatform(1);
            birinciValfComboBox.getItems().addAll(
                    "1",
                    "2"
            );
        } else {
            initCustomPlatform(0);
            birinciValfComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().valfTipiMap.get("0"));
        }
    }

    private void initCustomPlatform(int platformStatus) {
        if(platformStatus == 1) {
            birinciValfComboLabel.setText("Valf Sayısı");
            ikinciValfComboLabel.setText("Valf Tipi");
            birinciValfComboBox.setPromptText("Valf Sayısı");
            ikinciValfComboBox.setPromptText("Valf Tipi");
        } else {
            birinciValfComboLabel.setText("1. Valf Tipi");
            ikinciValfComboLabel.setText("2. Valf Tipi");
            birinciValfComboBox.setPromptText("1. Valf Tipi");
            ikinciValfComboBox.setPromptText("2. Valf Tipi");
        }
    }

    private void initInisMetodu() {
        inisMetoduComboBox.getItems().clear();
        inisMetoduComboBox.setDisable(false);
        inisMetoduComboBox.getItems().addAll("İnişte Tek Hız", "İnişte Çift Hız");
    }

    private void initIkinciValf() {
        ikinciValfComboBox.getItems().clear();
        ikinciValfComboBox.setDisable(false);
        if(secilenPlatformTipi.equals("Özel")) {
            if(secilenBirinciValf.equals("1")) {
                //Tek Valf
                if(secilenMotorTipi.equals("12 V (DC)") || secilenMotorTipi.equals("24 V (DC)")) {
                    //DC Motor
                    ikinciValfComboBox.getItems().addAll(
                            "J Merkez",
                            "H Merkez",
                            "Açık Merkez"
                    );
                } else {
                    //AC
                    ikinciValfComboBox.getItems().addAll(
                            "J Merkez",
                            "H Merkez"
                    );
                }
            } else {
                //Çift Valf
                ikinciValfComboBox.getItems().addAll(
                        "1. Valf: J Merkez\n2. Valf: Kapalı Merkez"
                );
            }
        } else {
            ikinciValfComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().valfTipiMap.get("0"));
            ikinciValfComboBox.getItems().addAll("Yok");
        }
    }

    public void hesaplaFunc() {
        if(checkComboBox()) {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Hesaplama Hatası", "Lütfen tüm girdileri kontrol edin.");
        } else {
            enableSonucSection();
            collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, true, false);
            hesaplamaBitti = true;
            
            // Ünite bilgileri bölümünü kapat
            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, true);
            isUnitInfoSectionExpanded = false;
            
            // Hesaplama sonucu bölümüne scroll yap
            Platform.runLater(() -> {
                if(mainScrollPane != null && calculationResultSectionContainer != null) {
                    mainScrollPane.requestLayout();
                    mainScrollPane.layout();
                    // Hesaplama sonucu bölümünün konumunu bul ve scroll yap
                    double targetY = calculationResultSectionContainer.getLayoutY();
                    double scrollPaneHeight = mainScrollPane.getViewportBounds().getHeight();
                    double contentHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight();
                    if(contentHeight > scrollPaneHeight && targetY > 0) {
                        double vvalue = targetY / (contentHeight - scrollPaneHeight);
                        mainScrollPane.setVvalue(Math.max(0.0, Math.min(1.0, vvalue)));
                    }
                }
            });
        }
    }

    private void enableSonucSection() {
        Image image = null;

        if(secilenPlatformTipi != null) {
            if(Objects.equals(secilenPlatformTipi, "ESP")) {
                if(Objects.equals(secilenInisTipi, "İnişte Tek Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/powerpack/tekhiz.png")));
                } else if(Objects.equals(secilenInisTipi, "İnişte Çift Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/powerpack/cifthiz.png")));
                }
            } else if(Objects.equals(secilenPlatformTipi, "Devirmeli + Yürüyüş")) {
                image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/powerpack/ozel.png")));
            } else if(Objects.equals(secilenPlatformTipi, "Özel")) {
                if(secilenBirinciValf != null && Objects.equals(secilenIkinciValf, "Yok")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/powerpack/tekvalf.png")));
                } else if(secilenIkinciValf != null) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/powerpack/ozel.png")));
                }
            }
        }

        tankImage.setImage(image);

        calculateKabin();
    }

    private void calculateKabin() {
        if(!secilenTankTipi.contains("Özel")) {
            String motorKW = secilenMotorGucu.trim();
            LinkedList<Motor> motorInfos = null;
            Optional<Kabin> selectedKabin = Optional.empty();

            if(uniteTipiDurumu.equals("Hidros")) {
                //Hidros için motor yüksekliğe göre kabin önerisi
                if(secilenMotorTipi.equals("380 V (AC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("0");
                } else if(secilenMotorTipi.equals("220 V (AC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("2");
                } else if(secilenMotorTipi.equals("24 V (DC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("5");
                } else if(secilenMotorTipi.equals("12 V (DC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("4");
                }
            } else {
                //İthal için motor yüksekliğe göre kabin önerisi
                if(secilenMotorTipi.equals("380 V (AC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("1");
                } else if(secilenMotorTipi.equals("220 V (AC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("3");
                } else if(secilenMotorTipi.equals("24 V (DC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("7");
                } else if(secilenMotorTipi.equals("12 V (DC)")) {
                    motorInfos = SystemDefaults.getLocalHydraulicData().motorGucuMap.get("6");
                }
            }

            for(Kabin currentCabin : SystemDefaults.getLocalHydraulicData().powerPackCabins) {
                String currentMotorHeight = motorInfos.get(0).getMotorYukseklik().replace(" mm", "");
                int currentMotorHeightVal = Integer.parseInt(currentMotorHeight);
                if(currentCabin.getKabinDisH() > currentMotorHeightVal) {
                    selectedKabin = Optional.of(currentCabin);
                    break;
                }
            }

            atananKabin = selectedKabin.get().kabinName;
            kabinKodu = selectedKabin.get().kabinKodu;

            tankTitle.setText("Kabin: " + atananKabin);
            tankOlculeriText.setText("Geçiş Ölçüleri: " + selectedKabin.get().gecisOlculeri + "\nKabin Kodu: " + selectedKabin.get().kabinKodu);
        } else {
            atananKabin = "Özel Kabin";
            tankTitle.setText("Kabin: " + atananKabin);
            tankOlculeriText.setText("Girilen Ölçüler: " + genislikTextField.getText() + "x" + yukseklikTextField.getText() + "x" + derinlikTextField.getText());
        }
    }

    private boolean checkComboBox() {
        boolean isSiparisNumarasiEmpty = isStringEmpty(siparisNumarasiField.getText());
        boolean isMotorComboBoxEmpty = isComboBoxEmpty(motorVoltajComboBox);
        boolean isMotorGucuComboBoxEmpty = isComboBoxEmpty(motorGucuComboBox);
        boolean isPompaComboBoxEmpty = isComboBoxEmpty(pompaComboBox);
        boolean isTankTipiComboBoxEmpty = isComboBoxEmpty(tankTipiComboBox);
        boolean isPlatformTipiComboBoxEmpty = isComboBoxEmpty(platformTipiComboBox);

        boolean isTankKapasitesiEmpty = isStringEmpty(secilenTankKapasitesi);
        boolean isOzelTankGenislikEmpty = isStringEmpty(secilenOzelTankGenislik);
        boolean isOzelTankYukseklikEmpty = isStringEmpty(secilenOzelTankYukseklik);
        boolean isOzelTankDerinlikEmpty = isStringEmpty(secilenOzelTankDerinlik);

        boolean isTankCapacityInvalid = isTankKapasitesiEmpty &&
                (isOzelTankGenislikEmpty || isOzelTankYukseklikEmpty || isOzelTankDerinlikEmpty);

        if (Objects.equals(secilenPlatformTipi, "ESP")) {
            boolean isInisTipiComboBoxEmpty = isComboBoxEmpty(inisMetoduComboBox);
            return isSiparisNumarasiEmpty ||
                    isMotorComboBoxEmpty ||
                    isMotorGucuComboBoxEmpty ||
                    isPompaComboBoxEmpty ||
                    isTankTipiComboBoxEmpty ||
                    isTankCapacityInvalid ||
                    isPlatformTipiComboBoxEmpty ||
                    isInisTipiComboBoxEmpty;
        } else if (Objects.equals(secilenPlatformTipi, "Devirmeli + Yürüyüş")) {
            return isSiparisNumarasiEmpty ||
                    isMotorComboBoxEmpty ||
                    isMotorGucuComboBoxEmpty ||
                    isPompaComboBoxEmpty ||
                    isTankTipiComboBoxEmpty ||
                    isTankCapacityInvalid ||
                    isPlatformTipiComboBoxEmpty;
        } else if (Objects.equals(secilenPlatformTipi, "Özel")) {
            boolean isBirinciValfComboBoxEmpty = isComboBoxEmpty(birinciValfComboBox);
            boolean isIkinciValfComboBoxEmpty = isComboBoxEmpty(ikinciValfComboBox);
            return isSiparisNumarasiEmpty ||
                    isMotorComboBoxEmpty ||
                    isMotorGucuComboBoxEmpty ||
                    isPompaComboBoxEmpty ||
                    isTankTipiComboBoxEmpty ||
                    isTankCapacityInvalid ||
                    isPlatformTipiComboBoxEmpty ||
                    isBirinciValfComboBoxEmpty ||
                    isIkinciValfComboBoxEmpty;
        }

        return false;
    }

    private boolean isStringEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isComboBoxEmpty(ComboBox<?> comboBox) {
        return comboBox.getSelectionModel() == null || comboBox.getSelectionModel().getSelectedItem() == null;
    }

    private void createAndLoadPartListTable() {
        String secilenPlatform = secilenPlatformTipi.trim();

        loadStockCodes();
        loadMotorParca();
        loadPompaParca();
        loadTankTipi();
        loadPlatformTipi();
        //loadGenelParcalar();
        if(secilenBirinciValf != null) {
            loadValfParcalar();
        }

        if(secilenPlatform.equals("Özel - Yatay")) {
            loadOzelYatayGenel();
        }

        loadManometre();
        loadBasincSalteri();
        loadElPompasiParca();
        if(secilenTankKapasitesi != null) {
            loadYagMiktari();
        }
    }

    private void loadMotorParca() {
        String motorGucu = PowerPackController.secilenMotorGucu.trim();

        if(PowerPackController.uniteTipiDurumu.equals("Hidros")) {
            //Hidros Malzeme Listesi
            if (Objects.equals(PowerPackController.secilenMotorTipi, "380 V (AC)")) {
                if(motorGucu.equals("0.37 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("0"), "Motor Parçaları");
                } else if(motorGucu.equals("0.55 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("1"), "Motor Parçaları");
                } else if(motorGucu.equals("0.75 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("2"), "Motor Parçaları");
                } else if(motorGucu.equals("1.1 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("3"), "Motor Parçaları");
                } else if(motorGucu.equals("1.5 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("4"), "Motor Parçaları");
                } else if(motorGucu.equals("2.2 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("5"), "Motor Parçaları");
                } else if(motorGucu.equals("3 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("6"), "Motor Parçaları");
                } else if(motorGucu.equals("4 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.get("7"), "Motor Parçaları");
                }
            } else if (Objects.equals(PowerPackController.secilenMotorTipi, "220 V (AC)")) {
                if(motorGucu.equals("0.37 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.get("0"), "Motor Parçaları");
                } else if(motorGucu.equals("0.55 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.get("1"), "Motor Parçaları");
                } else if(motorGucu.equals("0.75 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.get("2"), "Motor Parçaları");
                } else if(motorGucu.equals("1.1 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.get("3"), "Motor Parçaları");
                } else if(motorGucu.equals("1.5 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.get("4"), "Motor Parçaları");
                } else if(motorGucu.equals("2.2 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.get("5"), "Motor Parçaları");
                } else if(motorGucu.equals("3 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.get("6"), "Motor Parçaları");
                }
            }
        } else {
            //İthal Malzeme Listesi
            if (Objects.equals(PowerPackController.secilenMotorTipi, "380 V (AC)")) {
                if(motorGucu.equals("0.55 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.get("1"), "Motor Parçaları");
                } else if(motorGucu.equals("0.75 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.get("2"), "Motor Parçaları");
                } else if(motorGucu.equals("1.1 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.get("3"), "Motor Parçaları");
                } else if(motorGucu.equals("1.5 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.get("4"), "Motor Parçaları");
                } else if(motorGucu.equals("2.2 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.get("5"), "Motor Parçaları");
                } else if(motorGucu.equals("3 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.get("6"), "Motor Parçaları");
                } else if(motorGucu.equals("4 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.get("7"), "Motor Parçaları");
                }
            } else if (Objects.equals(PowerPackController.secilenMotorTipi, "220 V (AC)")) {
                if(motorGucu.equals("0.37 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.get("0"), "Motor Parçaları");
                } else if(motorGucu.equals("0.55 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.get("1"), "Motor Parçaları");
                } else if(motorGucu.equals("0.75 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.get("2"), "Motor Parçaları");
                } else if(motorGucu.equals("1.1 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.get("3"), "Motor Parçaları");
                } else if(motorGucu.equals("1.5 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.get("4"), "Motor Parçaları");
                } else if(motorGucu.equals("2.2 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.get("5"), "Motor Parçaları");
                } else if(motorGucu.equals("3 kW")) {
                    generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.get("6"), "Motor Parçaları");
                }
            }
        }
    }

    private void loadPompaParca() {
        String pompaDegeri = secilenPompa.trim();

        if(uniteTipiDurumu.equals("Hidros")) {
            //Hidros Malzeme Listesi
            if(pompaDegeri.equals("0.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("0"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("1.1 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("1"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("1.3 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("2"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("1.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("3"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("2.1 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("4"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("2.7 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("5"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("3.2 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("6"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("3.7 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("7"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("4.2 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("8"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("4.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("9"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("5.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("10"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("7 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("11"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("12"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("9 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.get("13"), "Pompa Parçaları");
            }
        } else {
            //İthal Malzeme Listesi
            if(pompaDegeri.equals("0.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("0"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("1.1 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("1"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("1.3 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("2"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("1.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("3"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("2.1 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("4"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("2.7 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("5"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("3.2 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("6"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("3.7 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("7"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("4.2 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("8"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("4.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("9"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("5.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("10"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("7 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("11"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("12"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("9 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("13"), "Pompa Parçaları");
            } else if(pompaDegeri.equals("9.8 cc")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.get("14"), "Pompa Parçaları");
            }
        }
    }

    private void loadTankTipi() {
        String kontrolTankTipi = secilenTankTipi.trim();

        if(secilenTankTipi.contains("Özel")) {
            PartListTable separatorData = new PartListTable("----", "Tank Parçaları", "----");
            partListTable.getItems().add(separatorData);

            String malzemeKodu = "Özel Tank";
            String malzemeAdi = "Genişlik: " + secilenOzelTankGenislik + "mm" + " Yükseklik: " + secilenOzelTankYukseklik + "mm" + " Derinlik: " + secilenOzelTankDerinlik + "mm";
            String adet = "1";

            PartListTable data = new PartListTable(malzemeKodu, malzemeAdi, adet);
            partListTable.getItems().add(data);
        } else {
            String kontrolTankKapasitesi = secilenTankKapasitesi.trim();

            if(uniteTipiDurumu.equals("Hidros")) {
                //Hidros Malzeme Listesi
                if(Objects.equals(kontrolTankTipi, "Yatay")) {
                    if(kontrolTankKapasitesi.equals("2 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.get("0"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("4 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.get("1"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("6 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.get("2"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("8 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.get("3"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("10 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.get("4"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("12 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.get("5"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("20 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.get("6"), "Tank Parçaları");
                    }
                } else if(Objects.equals(kontrolTankTipi, "Dikey")) {
                    if(kontrolTankKapasitesi.equals("4 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankDikey.get("0"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("6 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankDikey.get("1"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("8 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankDikey.get("2"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("10 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankDikey.get("3"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("12 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankDikey.get("4"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("20 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankDikey.get("5"), "Tank Parçaları");
                    }
                }
            } else {
                //İthal Malzeme Listesi
                if(Objects.equals(kontrolTankTipi, "Yatay")) {
                    if(kontrolTankKapasitesi.equals("2.5 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankYatay.get("0"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("3.8 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankYatay.get("1"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("7 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankYatay.get("2"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("10 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankYatay.get("3"), "Tank Parçaları");
                    }
                } else if(Objects.equals(kontrolTankTipi, "Dikey")) {
                    if(kontrolTankKapasitesi.equals("2.5 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("0"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("3.8 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("1"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("7 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("2"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("10 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("3"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("12 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("4"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("15 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("5"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("20 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("6"), "Tank Parçaları");
                    } else if(kontrolTankKapasitesi.equals("30 Lt")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.get("7"), "Tank Parçaları");
                    }
                }
            }
        }
    }

    private void loadPlatformTipi() {
        String secilenPlatform = secilenPlatformTipi.trim();

        if(uniteTipiDurumu.equals("Hidros")) {
            //Hidros Malzeme Listesi
            if(Objects.equals(secilenPlatform, "ESP")) {
                String secilenTank = secilenTankTipi.trim();

                if(secilenTank.contains("Dikey")) {
                    String secilenInis = secilenInisTipi.trim();

                    if(Objects.equals(secilenInis, "İnişte Tek Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPGenel.get("0"), "Platform Parçaları");
                    } else if(Objects.equals(secilenInis, "İnişte Çift Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPGenel.get("0"), "Platform Parçaları");
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPCiftHiz.get("0"), "Platform Parçaları");
                    }
                } else if(secilenTank.contains("Yatay")) {
                    String secilenInis = secilenInisTipi.trim();

                    if(Objects.equals(secilenInis, "İnişte Tek Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPGenel.get("0"), "Platform Parçaları");
                    } else if(Objects.equals(secilenInis, "İnişte Çift Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPGenel.get("0"), "Platform Parçaları");
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPCiftHiz.get("0"), "Platform Parçaları");
                    }
                }
            } else if(Objects.equals(secilenPlatform, "Devirmeli + Yürüyüş")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaDevirmeli.get("0"), "Platform Parçaları");
            }
        } else {
            //İthal Malzeme Listesi
            if(Objects.equals(secilenPlatform, "ESP")) {
                String secilenTank = secilenTankTipi.trim();

                if(secilenTank.contains("Dikey")) {
                    String secilenInis = secilenInisTipi.trim();

                    if(Objects.equals(secilenInis, "İnişte Tek Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPGenel.get("0"), "Platform Parçaları");
                    } else if(Objects.equals(secilenInis, "İnişte Çift Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPGenel.get("0"), "Platform Parçaları");
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPCiftHiz.get("0"), "Platform Parçaları");
                    }
                } else if(secilenTank.contains("Yatay")) {
                    String secilenInis = secilenInisTipi.trim();

                    if(Objects.equals(secilenInis, "İnişte Tek Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPGenel.get("0"), "Platform Parçaları");
                    } else if(Objects.equals(secilenInis, "İnişte Çift Hız")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPGenel.get("0"), "Platform Parçaları");
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPCiftHiz.get("0"), "Platform Parçaları");
                    }
                }
            } else if(Objects.equals(secilenPlatform, "Devirmeli + Yürüyüş")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaDevirmeli.get("0"), "Platform Parçaları");
            }
        }
    }

    private void loadManometre() {
        PartListTable separatorData = new PartListTable("----", "Manometre Parçaları", "----");
        partListTable.getItems().add(separatorData);

        if(Objects.equals(manometreDurumu, "Var")) {
            String malzemeKodu = "150-51-10-802";
            String secilenMalzeme = "Manometre";
            String adet = "1";

            PartListTable data = new PartListTable(malzemeKodu, secilenMalzeme, adet);
            partListTable.getItems().add(data);
        }
    }

    private void loadBasincSalteri() {
        PartListTable separatorData = new PartListTable("----", "Basınç Şalteri Parçaları", "----");
        partListTable.getItems().add(separatorData);

        if(Objects.equals(basincSalteriDurumu, "Var")) {
            String malzemeKodu = "150-51-10-457";
            String secilenMalzeme = "Basınç Şalteri";
            String adet = "1";

            PartListTable data = new PartListTable(malzemeKodu, secilenMalzeme, adet);
            partListTable.getItems().add(data);
        }
    }

    private void loadElPompasiParca() {
        PartListTable separatorData = new PartListTable("----", "El Pompası Parçaları", "----");
        partListTable.getItems().add(separatorData);

        if(Objects.equals(elPompasiDurumu, "Var")) {
            String malzemeKodu = "150-51-05-007";
            String secilenMalzeme = "A11 EL POMPALI BLOK V BLOK";
            String adet = "1";

            String malzemeKodu2 = "150-51-05-059";
            String secilenMalzeme2 = "A01 BLOK";
            String adet2 = "1";

            PartListTable data = new PartListTable(malzemeKodu, secilenMalzeme, adet);
            PartListTable data2 = new PartListTable(malzemeKodu2, secilenMalzeme2, adet2);
            partListTable.getItems().add(data);
            partListTable.getItems().add(data2);
        }
    }

    private void loadGenelParcalar() {
        if(uniteTipiDurumu.equals("Hidros")) {
            //Hidros Malzeme Listesi
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaDefault.get("0"), "Standart Parçalar");
        } else {
            //İthal Malzeme Listesi
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaDefault.get("0"), "Standart Parçalar");
        }
    }

    private void loadOzelYatayGenel() {
        if(uniteTipiDurumu.equals("Hidros")) {
            //Hidros Malzeme Listesi
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaOzelYatayGenel.get("0"), "Özel Yatay Genel Parçalar");
        } else {
            //İthal Malzeme Listesi
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaOzelYatayGenel.get("0"), "Özel Yatay Genel Parçalar");
        }
    }

    private void loadYagMiktari() {
        PartListTable separatorData = new PartListTable("----", "Hidrolik Yağ Parçaları", "----");
        partListTable.getItems().add(separatorData);

        String tankKapasite = PowerPackController.secilenTankKapasitesi.trim();

        String malzemeKodu = "150-53-04-002";
        String malzemeAdi = "HİDROLİK YAĞ SHELL TELLUS S2 M46";
        String adet = "";

        if(tankKapasite.equals("2 Lt")) {
            adet = "2 Lt";
        } else if(tankKapasite.equals("4 Lt")) {
            adet = "4 Lt";
        } else if(tankKapasite.equals("6 Lt")) {
            adet = "6 Lt";
        } else if(tankKapasite.equals("8 Lt")) {
            adet = "8 Lt";
        } else if(tankKapasite.equals("10 Lt")) {
            adet = "10 Lt";
        } else if(tankKapasite.equals("12 Lt")) {
            adet = "12 Lt";
        } else if(tankKapasite.equals("20 Lt")) {
            adet = "20 Lt";
        }

        PartListTable data = new PartListTable(malzemeKodu, malzemeAdi, adet);
        partListTable.getItems().add(data);
    }

    private void loadStockCodes() {
        String adet = "1";
        List<PartListTable> dataList;

        if(atananKabin.equals("Özel Kabin")) {
            dataList = Arrays.asList(
                    new PartListTable("----", "Kabin Genel Bilgisi", "----"),
                    new PartListTable("----", atananKabin, adet)
            );
        } else {
            Kabin foundedTank = Utils.findPowerPackTankByKabinName(atananKabin);
            dataList = Arrays.asList(
                    new PartListTable("----", "Kabin Genel Bilgisi", "----"),
                    new PartListTable(foundedTank.getKabinKodu(), foundedTank.getMalzemeAdi(), adet)
            );
        }

        partListTable.getItems().addAll(dataList);
    }

    private void loadValfParcalar() {
        if(uniteTipiDurumu.equals("Hidros")) {
            //Hidros Malzeme Listesi
            if(secilenPlatformTipi.contains("Özel")) {
                if(secilenBirinciValf.equals("1")) {
                    if(secilenIkinciValf.equals("Açık Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("0"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("1"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("J Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("2"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("3"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("H Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("4"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("5"), "Valf Parçaları");
                        }
                    }
                } else {
                    loadOzelCiftValf();
                }
            } else {
                if(secilenBirinciValf.equals("Açık Merkez")) {
                    if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("0"), "Valf Parçaları");
                    } else {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("1"), "Valf Parçaları");
                    }
                } else if(secilenBirinciValf.equals("J Merkez")) {
                    if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("2"), "Valf Parçaları");
                    } else {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("3"), "Valf Parçaları");
                    }
                } else if(secilenBirinciValf.equals("H Merkez")) {
                    if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("4"), "Valf Parçaları");
                    } else {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("5"), "Valf Parçaları");
                    }
                }

                if(secilenIkinciValf != null) {
                    if(secilenIkinciValf.equals("Açık Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("0"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("1"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("J Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("2"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("3"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("H Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("4"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.get("5"), "Valf Parçaları");
                        }
                    }
                }
            }
        } else {
            //İthal Malzeme Listesi
            if(secilenPlatformTipi.contains("Özel")) {
                if(secilenBirinciValf.equals("1")) {
                    if(secilenIkinciValf.equals("Açık Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("0"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("1"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("J Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("2"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("3"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("H Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("4"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("5"), "Valf Parçaları");
                        }
                    }
                } else {
                    loadOzelCiftValf();
                }
            } else {
                if(secilenBirinciValf.equals("Açık Merkez")) {
                    if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("0"), "Valf Parçaları");
                    } else {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("1"), "Valf Parçaları");
                    }
                } else if(secilenBirinciValf.equals("J Merkez")) {
                    if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("2"), "Valf Parçaları");
                    } else {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("3"), "Valf Parçaları");
                    }
                } else if(secilenBirinciValf.equals("H Merkez")) {
                    if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("4"), "Valf Parçaları");
                    } else {
                        generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("5"), "Valf Parçaları");
                    }
                }

                if(secilenIkinciValf != null) {
                    if(secilenIkinciValf.equals("Açık Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("0"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("1"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("J Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("2"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("3"), "Valf Parçaları");
                        }
                    } else if(secilenIkinciValf.equals("H Merkez")) {
                        if(!PowerPackController.secilenMotorTipi.contains("12 V")) {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("4"), "Valf Parçaları");
                        } else {
                            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.get("5"), "Valf Parçaları");
                        }
                    }
                }
            }
        }
    }

    private void loadOzelCiftValf() {
        if(uniteTipiDurumu.equals("Hidros")) {
            //Hidros Malzeme Listesi
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaOzelCiftValf.get("0"), "Özel Çift Valf Parçaları");
        } else {
            //İthal Malzeme Listesi
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().powerPackIthalParcaOzelCiftValf.get("0"), "Özel Çift Valf Parçaları");
        }
    }

    public void exportSchemeProcess() {
        int startX = 800;
        int startY = 270;
        int width = 370;
        int height = 430;

        if(hesaplamaBitti) {
            String generalCyclinderString = silindirSayisiCombo.getSelectionModel().getSelectedItem().toString();
            String numberPart = "";
            String stringPart = "";
            if(generalCyclinderString != null) {
                numberPart = generalCyclinderString.replaceAll("[^0-9]", "");
                stringPart = generalCyclinderString.replaceAll("[0-9]", "");
            } else {
                NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Lütfen silindir sayısı seçin.");
                return;
            }

            int selectedCylinders = Integer.parseInt(numberPart);
            String isPressureValf = stringPart;
            if (selectedCylinders == -1 && isPressureValf.isEmpty()) {
                NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Lütfen silindir sayısı seçin.");
                return;
            }

            String pdfPath = hydraulicSchemaSelection(selectedCylinders, isPressureValf);
            System.out.println("PDF Şema Yolu: " + pdfPath);

            PDFUtil.pdfGenerator("/assets/images/logos/onderlift-logo.png", tankImagePaneSection, null, "/assets/data/hydraulicUnitData/schematicPDF/powerpack/" + pdfPath, girilenSiparisNumarasi, tankTitle.getText(), secilenMotorTipi, secilenPompa, secilenUniteTipi, false);
            // PDF üretildikten sonra "Dosyada Göster" butonunu görünür yap
            if(openPDFInExplorerButton != null) {
                openPDFInExplorerButton.setVisible(true);
                openPDFInExplorerButton.setManaged(true);
            }
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    PDFUtil.loadPDFPagesToImageViews(SystemDefaults.userDataPDFFolderPath + girilenSiparisNumarasi + ".pdf", schemePageOne, schemePageTwo);
                    return null;
                }
            };

            task.setOnFailed(event -> {
                Throwable exception = task.getException();
                exception.printStackTrace();
            });

            task.setOnSucceeded(event -> {
                schemePageOne.setVisible(true);
                schemePageOne.setFitHeight(600.0);
                schemePageTwo.setVisible(true);
                schemePageTwo.setFitHeight(600.0);
                System.out.println("PDF sayfaları başarıyla yüklendi.");
            });

            Platform.runLater(() -> {
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            });
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Lütfen hesaplama işlemini tamamlayıp tekrar deneyin.");
        }
    }

    private String hydraulicSchemaSelection(int selectedCylinders, String isPressureValf) {
        if(secilenPlatformTipi.equals("ESP")) {
            if(secilenInisTipi.equals("İnişte Tek Hız")) {
                return getCylinderImage(selectedCylinders, isPressureValf, 1, 2, 3, 4);
            } else if(secilenInisTipi.equals("İnişte Çift Hız")) {
                return getCylinderImage(selectedCylinders, isPressureValf, 5, 6, 7, 8);
            }
        } else if(secilenPlatformTipi.equals("Devirmeli + Yürüyüş")) {
            return getCylinderImage(selectedCylinders, isPressureValf, 9, 10, 11, 12);
        } else if(secilenPlatformTipi.equals("Özel")) {
            if(secilenBirinciValf.equals("1")) {
                if(secilenIkinciValf.equals("J Merkez")) {
                    return getCylinderImage(selectedCylinders, isPressureValf, 13, 14, 15, 16);
                } else if(secilenIkinciValf.equals("H Merkez")) {
                    return getCylinderImage(selectedCylinders, isPressureValf, 17, 18, 19, 20);
                }
            } else {
                return getCylinderImage(selectedCylinders, isPressureValf, 21, 22, 23, 24);
            }
        }

        return null;
    }

    private String getCylinderImage(int selectedCylinders, String isPressureValf, int one, int two, int three, int other) {
        String suffix = isPressureValf.equals("Var") ? "B" : "";
        switch (selectedCylinders) {
            case 1:
                return one + suffix + ".pdf";
            case 2:
                return two + suffix + ".pdf";
            case 3:
                return three + suffix + ".pdf";
            default:
                return other + suffix + ".pdf";
        }
    }

    private void generalLoadFunc(LinkedList<String> parcaListesi, String seperatorText) {
        PartListTable separatorData = new PartListTable("----", seperatorText, "----");
        partListTable.getItems().add(separatorData);

        for (String veri : parcaListesi) {
            String[] veriParcalari = veri.split(";");

            String malzemeKodu = veriParcalari[0];
            String secilenMalzeme = veriParcalari[1];
            String adet = veriParcalari[2];

            PartListTable data = new PartListTable(malzemeKodu, secilenMalzeme, adet);
            partListTable.getItems().add(data);
        }

        partListTable.setRowFactory(tv -> new TableRow<>() {
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

    private void clearComboBoxSelection(ComboBox targetCombo) {
        if(targetCombo.getSelectionModel().getSelectedItem() != null) {
            targetCombo.setDisable(true);
            targetCombo.getItems().set(targetCombo.getSelectionModel().getSelectedIndex(), targetCombo.getPromptText());
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
    
    @FXML
    public void handleOrderSectionClick(MouseEvent event) {
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return;
        }
        collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false, false);
        isOrderSectionExpanded = !isOrderSectionExpanded;
    }
    
    @FXML
    public void handleUnitInfoSectionClick(MouseEvent event) {
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return;
        }
        collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, false);
        isUnitInfoSectionExpanded = !isUnitInfoSectionExpanded;
    }
    
    @FXML
    public void handleCalculationResultSectionClick(MouseEvent event) {
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return;
        }
        if(hesaplamaBitti) {
            collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false, false);
            isCalculationResultSectionExpanded = !isCalculationResultSectionExpanded;
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Hesaplama sonucunu görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
        }
    }
    
    @FXML
    public void handlePartListSectionClick(MouseEvent event) {
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return;
        }
        if(hesaplamaBitti) {
            collapseAndExpandSection(partListSection, isPartListSectionExpanded, partListSectionButtonImage, false, false);
            isPartListSectionExpanded = !isPartListSectionExpanded;
            
            if(!isPartListSectionExpanded) {
                basincSalteriCombo.setDisable(false);
                basincSalteriCombo.getItems().clear();
                basincSalteriCombo.getItems().addAll("Var", "Yok");
            }
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Parça listesini görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
        }
    }
    
    @FXML
    public void handleUnitSchemeSectionClick(MouseEvent event) {
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return;
        }
        if(hesaplamaBitti) {
            collapseAndExpandSection(unitSchemeSection, isUnitSchemeSectionExpanded, unitSchemeSectionButtonImage, false, false);
            isUnitSchemeSectionExpanded = !isUnitSchemeSectionExpanded;
            
            if(!isUnitSchemeSectionExpanded) {
                basincSalteriSchemeCombo.setDisable(false);
                basincSalteriSchemeCombo.getItems().clear();
                basincSalteriSchemeCombo.getItems().addAll("Var", "Yok");
            }
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Şema Hatası", "Ünite şemasını görüntüleyebilmeniz için önce hesaplamayı bitirmeniz gerek.");
        }
    }
    
    @FXML
    public void handleCalculationControlSectionClick(MouseEvent event) {
        if(event.getTarget() instanceof Button || event.getTarget() instanceof ImageView) {
            return;
        }
        collapseAndExpandSection(calculationControlSection, isCalculationControlSectionExpanded, calculationControlSectionButtonImage, false, false);
        isCalculationControlSectionExpanded = !isCalculationControlSectionExpanded;
    }
    
    @FXML
    public void stopEventPropagation(MouseEvent event) {
        event.consume();
    }
    
    @FXML
    public void handleInviteUserClick(MouseEvent event) {
        NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), 
            NotificationController.NotificationType.WARNING, 
            "Ana sunucuya bağlanılamadı", 
            "Lütfen geliştirici ile iletişime geçin.\nhidirektor@gmail.com");
    }
}
