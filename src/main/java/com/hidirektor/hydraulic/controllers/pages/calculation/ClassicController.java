package com.hidirektor.hydraulic.controllers.pages.calculation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.controllers.notification.NotificationController;
import com.hidirektor.hydraulic.utils.File.PDF.PDFUtil;
import com.hidirektor.hydraulic.utils.Model.Hydraulic.Kabin;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import me.t3sl4.util.os.desktop.DesktopUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ClassicController implements Initializable  {

    @FXML
    public Label classicCaclulationTitle;

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

    //Hesaplama alanları:
    @FXML
    public TextField siparisNumarasiField, inviteUserTextField;

    @FXML
    public ComboBox<String> motorComboBox, sogutmaComboBox, hidrolikKilitComboBox, pompaComboBox, kompanzasyonComboBox, valfTipiComboBox, kilitMotorComboBox, kilitPompaComboBox;

    @FXML
    public TextField gerekenYagMiktariField;

    boolean isOrderSectionExpanded = false, isUnitInfoSectionExpanded = false, isCalculationResultSectionExpanded = false, isPartListSectionExpanded = false, isUnitSchemeSectionExpanded = false, isCalculationControlSectionExpanded = false;

    /*
    Sonuç için label ve imagelar
     */

    @FXML
    public Label tankTitle, tankOlculeriText, kabinTitle, disOlculerLabel, gecisOlculeriLabel, tankOlculeriLabel;

    @FXML
    public ImageView tankImage, schemeImage;

    @FXML
    public AnchorPane tankImagePaneSection, hydraulicUnitSchemePane;

    /*
    Parça listesi componentleri
     */
    @FXML
    public ComboBox basincSalteriCombo, elPompasiCombo;

    @FXML
    private TableView<PartListTable> partListTable;

    @FXML
    private TableColumn<PartListTable, String> malzemeKodu;

    @FXML
    private TableColumn<PartListTable, String> secilenMalzeme;

    @FXML
    private TableColumn<PartListTable, String> adet;

    private String basincSalteriDurumu = null;
    private String elPompasiDurumu = null;

    /*
    Ünite Şeması Componentleri
     */
    @FXML
    public ComboBox<String> basincSalteriSchemeCombo, silindirSayisiCombo;

    @FXML
    public ImageView schemePageOne, schemePageTwo;

    private String basincSalteriSchemeDurumu = null;
    private String silindirSayisi = null;

    /*
    Clear Section
     */
    @FXML
    public Button clearButton;

    @FXML
    public ImageView clearButtonImage;


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

    //Hesaplama Sonucu için Değişkenler:
    public static boolean hesaplamaBitti = false;
    public static String atananHT;
    private ArrayList<Text> sonucTexts = new ArrayList<>();
    public static String atananKabinFinal = "";
    public static String gecisOlculeriFinal = "";
    private String imagePath = "";
    private String reverseImagePath = "";
    int motorYukseklik = 0;
    int calculationResultX, calculationResultY, calculationResultH;

    //Sonuç Kontrol Tablosu
    @FXML
    private TableView<DataControlTable> sonucTablo;

    @FXML
    private TableColumn<DataControlTable, String> dataKeyLine;

    @FXML
    private TableColumn<DataControlTable, String> dataValueLine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ValidationUtil.applyValidation(gerekenYagMiktariField, ValidationUtil.ValidationType.NUMERIC);
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

                basincSalteriCombo.setDisable(false);
                basincSalteriCombo.getItems().clear();
                basincSalteriCombo.getItems().addAll("Var", "Yok");
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
        String excelFileName = SystemDefaults.userDataExcelFolderPath + ClassicController.girilenSiparisNumarasi + ".xlsx";

        Multimap<String, PartListTable> malzemeMultimap = LinkedListMultimap.create();
        Multimap<String, PartListTable> filteredMultimap = LinkedListMultimap.create();
        Multimap<String, PartListTable> duplicateMultimap = LinkedListMultimap.create();

        for (PartListTable rowData : veriler) {
            if (!(rowData.getMalzemeKoduProperty().equals("----") && rowData.getMalzemeAdetProperty().equals("----"))) {
                String malzemeKey = rowData.getMalzemeKoduProperty();
                String mapKey = rowData.getMalzemeKoduProperty() + " " + rowData.getMalzemeAdiProperty();

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
                jsonObject.put("Ünite Tipi", ClassicController.secilenUniteTipi);
                jsonObject.put("Sipariş Numarası", ClassicController.girilenSiparisNumarasi);
                jsonObject.put("Motor", ClassicController.secilenMotor);
                jsonObject.put("Soğutma", ClassicController.secilenSogutmaDurumu);
                jsonObject.put("Hidrolik Kilit", ClassicController.secilenHidrolikKilitDurumu);
                jsonObject.put("Pompa", ClassicController.secilenPompa);
                jsonObject.put("Gerekli Yağ Miktarı", ClassicController.girilenTankKapasitesiMiktari);
                jsonObject.put("Kompanzasyon", ClassicController.kompanzasyonDurumu);
                jsonObject.put("Valf Tipi", ClassicController.secilenValfTipi);
                jsonObject.put("Kilit Motor", ClassicController.secilenKilitMotor);
                jsonObject.put("Kilit Pompa", ClassicController.secilenKilitPompa);
                jsonObject.put("Seçilen Kampana", ClassicController.secilenKampana);
                jsonObject.put("Seçilen Pompa Val", ClassicController.secilenPompaVal);

                if(SystemDefaults.loggedInUser != null) {
                    Utils.createLocalUnitData(SystemDefaults.userLocalUnitDataFilePath,
                            ClassicController.girilenSiparisNumarasi,
                            Utils.getCurrentUnixTime(),
                            ClassicController.secilenUniteTipi,
                            null,
                            excelFileName,
                            "no",
                            SystemDefaults.loggedInUser.getUserID(),
                            jsonObject);
                } else {
                    Utils.createLocalUnitData(SystemDefaults.userLocalUnitDataFilePath,
                            ClassicController.girilenSiparisNumarasi,
                            Utils.getCurrentUnixTime(),
                            ClassicController.secilenUniteTipi,
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

        clearComboBoxSelection(motorComboBox);
        secilenMotor = null;
        secilenKampana = 0;

        clearComboBoxSelection(sogutmaComboBox);
        secilenSogutmaDurumu = null;

        clearComboBoxSelection(hidrolikKilitComboBox);
        secilenHidrolikKilitDurumu = null;

        clearComboBoxSelection(pompaComboBox);
        secilenPompa = null;
        secilenPompaVal = 0;

        gerekenYagMiktariField.clear();
        gerekenYagMiktariField.setDisable(true);
        girilenTankKapasitesiMiktari = 0;

        clearComboBoxSelection(kompanzasyonComboBox);
        kompanzasyonDurumu = null;

        clearComboBoxSelection(valfTipiComboBox);
        secilenValfTipi = null;

        clearComboBoxSelection(kilitMotorComboBox);
        secilenKilitMotor = null;

        clearComboBoxSelection(kilitPompaComboBox);
        secilenKilitPompa = null;

        sonucTablo.getItems().clear();
        tankTitle.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        tankOlculeriText.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        kabinTitle.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        disOlculerLabel.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        gecisOlculeriLabel.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        tankOlculeriLabel.setText("Lütfen önce hesaplama işlemini tamamlayın.");
        tankImage.setImage(null);
        schemeImage.setImage(null);

        partListTable.getItems().clear();

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

        imageTextDisable();
        
        // "Dosyada Göster" butonunu gizle
        if(openPDFInExplorerButton != null) {
            openPDFInExplorerButton.setVisible(false);
        }

        hesaplamaBitti = false;
    }

    private void clearComboBoxSelection(ComboBox targetCombo) {
        if(targetCombo.getSelectionModel().getSelectedItem() != null) {
            targetCombo.setDisable(true);
            targetCombo.getItems().set(targetCombo.getSelectionModel().getSelectedIndex(), targetCombo.getPromptText());
        }
    }

    private void comboBoxListener() {
        UIProcess.changeInputDataForTextField(siparisNumarasiField, newValue -> {
            girilenSiparisNumarasi = newValue;
            dataInit("motor", null);
            tabloGuncelle();

            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, true, false);
        });

        UIProcess.changeInputDataForComboBox(motorComboBox, newValue -> {
            secilenMotor = newValue;
            secilenKampana = Integer.parseInt(SystemDefaults.getLocalHydraulicData().motorKampanaMap.get(motorComboBox.getSelectionModel().getSelectedItem().toString()).replace(" mm", ""));

            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false, true);

            dataInit("sogutma", null);

            tabloGuncelle();
        }, null);


        UIProcess.changeInputDataForComboBox(sogutmaComboBox, newValue -> {
            secilenSogutmaDurumu = newValue;

            if(secilenSogutmaDurumu != null && secilenHidrolikKilitDurumu != null && secilenPompa != null && kompanzasyonDurumu != null) {
                initValfValues();
            } else {
                dataInit("hidrolikKilit", null);
            }

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(hidrolikKilitComboBox, newValue -> {
            secilenHidrolikKilitDurumu = newValue;

            if(secilenSogutmaDurumu != null && secilenHidrolikKilitDurumu != null && secilenPompa != null && kompanzasyonDurumu != null) {
                initValfValues();
            } else {
                dataInit("pompa", null);
            }

            tabloGuncelle();
        }, null);

        UIProcess.changeInputDataForComboBox(pompaComboBox, newValue -> {
            secilenPompa = newValue;
            secilenPompaVal = Utils.stringToDouble(secilenPompa);

            if(secilenSogutmaDurumu != null && secilenHidrolikKilitDurumu != null && secilenPompa != null && kompanzasyonDurumu != null) {
                initValfValues();
            } else {
                dataInit("yagMiktari", null);
            }

            tabloGuncelle();
        }, () -> imageTextDisable());

        UIProcess.changeInputDataForTextField(gerekenYagMiktariField, newValue -> {
            girilenTankKapasitesiMiktari = Integer.parseInt(newValue);

            dataInit("kompanzasyon", null);

            tabloGuncelle();
        });

        UIProcess.changeInputDataForComboBox(kompanzasyonComboBox, newValue -> {
            kompanzasyonDurumu = newValue;

            if(secilenSogutmaDurumu != null && secilenHidrolikKilitDurumu != null && secilenPompa != null && kompanzasyonDurumu != null) {
                initValfValues();
            }

            tabloGuncelle();
        }, () -> imageTextDisable());

        UIProcess.changeInputDataForComboBox(valfTipiComboBox, newValue -> {
            secilenValfTipi = newValue;

            if(secilenSogutmaDurumu.equals("Yok") && Objects.equals(secilenHidrolikKilitDurumu, "Var") && secilenPompaVal > 28.1) {
                dataInit("kilitMotor", null);
            } else if(secilenSogutmaDurumu.equals("Yok") && secilenHidrolikKilitDurumu.equals("Var") && kompanzasyonDurumu.equals("Var")) {
                dataInit("kilitMotor", 0);
            } else {
                hesaplaFunc();
            }

            tabloGuncelle();
        }, () -> imageTextDisable());

        UIProcess.changeInputDataForComboBox(kilitMotorComboBox, newValue -> {
            secilenKilitMotor = kilitMotorComboBox.getValue();

            dataInit("kilitPompa", null);

            tabloGuncelle();
        }, () -> imageTextDisable());

        UIProcess.changeInputDataForComboBox(kilitPompaComboBox, newValue -> {
            secilenKilitPompa = kilitPompaComboBox.getValue();

            hesaplaFunc();

            tabloGuncelle();
        }, () -> imageTextDisable());

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

    private boolean checkComboBox() {
        if(siparisNumarasiField.getText().isEmpty() || motorComboBox.getSelectionModel().isEmpty() || kompanzasyonComboBox.getSelectionModel().isEmpty() || pompaComboBox.getSelectionModel().isEmpty() || valfTipiComboBox.getSelectionModel().isEmpty() || hidrolikKilitComboBox.getSelectionModel().isEmpty() || sogutmaComboBox.getSelectionModel().isEmpty()) {
            return true;
        }
        int girilenTankKapasitesi = 0;
        girilenTankKapasitesi = Integer.parseInt(gerekenYagMiktariField.getText());

        if(gerekenYagMiktariField.getText() == null || girilenTankKapasitesi == 0) {
            return true;
        } else return girilenTankKapasitesi < 1 || girilenTankKapasitesi > 500;
    }

    public void hesaplaFunc() {
        ArrayList<Integer> results;
        int calculatedX, calculatedY, calculatedH, calculatedHacim;

        if (checkComboBox()) {
            NotificationUtil.showNotification(classicCaclulationTitle.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Hesaplama Hatası", "Lütfen tüm girdileri kontrol edin.");
        } else {
            imageTextDisable();

            enableSonucSection();
            results = calcDimensions();

            if (results.size() == 4) {
                calculatedX = results.get(0);
                calculatedY = results.get(1);
                calculatedH = results.get(2);
                calculatedHacim = results.get(3);

                tankTitle.setText("Tank : " + calculatedHacim + "L" + " (" + atananHT + ")");
                tankOlculeriText.setText("X: " + calculatedX + " mm " + "Y: " + calculatedY + " mm " + "h: " + calculatedH + " mm");

                tabloGuncelle();
                Image image = null;

                if(secilenSogutmaDurumu.equals("Var")) {
                    if (secilenHidrolikKilitDurumu.equals("Var")) {
                        //Hidrolik Kilit Var
                        if(secilenValfTipi.equals("İnişte Tek Hız") || secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                            image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitli_tek_hiz_black.png")));
                            imagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitli_tek_hiz_black.png";
                            reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitli_tek_hiz_black.png";
                            imageTextEnable(calculatedX, calculatedY, "sogutma_kilitli_tek_hiz");
                        } else if(secilenValfTipi.equals("İnişte Çift Hız") || secilenValfTipi.equals("Kilitli Blok")) {
                            image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitli_cift_hiz_black.png")));
                            imagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitli_cift_hiz_black.png";
                            reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitli_cift_hiz_black.png";
                            imageTextEnable(calculatedX, calculatedY, "sogutma_kilitli_cift_hiz");
                        }
                    } else {
                        //Hidrolik Kilit Yok
                        if(secilenValfTipi.equals("İnişte Tek Hız") || secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                            image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitsiz_tek_hiz_black.png")));
                            imagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitsiz_tek_hiz_black.png";
                            reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitsiz_tek_hiz_black.png";
                            imageTextEnable(calculatedX, calculatedY, "sogutma_kilitsiz_tek_hiz");
                        } else if(secilenValfTipi.equals("İnişte Çift Hız") || secilenValfTipi.equals("Kilitli Blok")) {
                            image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitsiz_cift_hiz_black.png")));
                            imagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitsiz_cift_hiz_black.png";
                            reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/sogutma_kilitsiz_cift_hiz_black.png";
                            imageTextEnable(calculatedX, calculatedY, "sogutma_kilitsiz_cift_hiz");
                        }
                    }
                } else {
                    if(secilenHidrolikKilitDurumu.equals("Var")) {
                        //Hidrolik Kilit Var
                        if(secilenValfTipi.equals("Kilitli Blok")) {
                            image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/kilitli_blok_black.png")));
                            imagePath = "/assets/data/hydraulicUnitData/schematicImages/kilitli_blok_black.png";
                            reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/kilitli_blok_black.png";
                            imageTextEnable(calculatedX, calculatedY, "kilitli_blok");
                        } else {
                            if(secilenKilitMotor != null) {
                                if(secilenValfTipi.equals("İnişte Tek Hız") || secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/kilit_ayri_tek_hiz_black.png")));
                                    imagePath = "/assets/data/hydraulicUnitData/schematicImages/kilit_ayri_tek_hiz_black.png";
                                    reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/kilit_ayri_tek_hiz_black.png";
                                    imageTextEnable(calculatedX, calculatedY, "kilit_ayri_tek_hiz");
                                } else if(secilenValfTipi.equals("İnişte Çift Hız")) {
                                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/kilit_ayri_cift_hiz_black.png")));
                                    imagePath = "/assets/data/hydraulicUnitData/schematicImages/kilit_ayri_cift_hiz_black.png";
                                    reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/kilit_ayri_cift_hiz_black.png";
                                    imageTextEnable(calculatedX, calculatedY, "kilit_ayri_cift_hiz");
                                }
                            }
                        }
                    } else {
                        //Hidrolik Kilit Yok
                        if(kompanzasyonDurumu.equals("Var")) {
                            //Kompanzasyon Var
                            image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/tek_hiz_kompanzasyon_arti_tek_hiz_black.png")));
                            imagePath = "/assets/data/hydraulicUnitData/schematicImages/tek_hiz_kompanzasyon_arti_tek_hiz_black.png";
                            reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/tek_hiz_kompanzasyon_arti_tek_hiz_black.png";
                            imageTextEnable(calculatedX, calculatedY, "tek_hiz_kompanzasyon_arti_tek_hiz");
                        } else {
                            if(secilenValfTipi.equals("İnişte Çift Hız")) {
                                image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/cift_hiz_black.png")));
                                imagePath = "/assets/data/hydraulicUnitData/schematicImages/cift_hiz_black.png";
                                reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/cift_hiz_black.png";
                                imageTextEnable(calculatedX, calculatedY, "cift_hiz");
                            } else if(secilenValfTipi.equals("İnişte Tek Hız")) {
                                image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/schematicImages/tek_hiz_kompanzasyon_arti_tek_hiz_black.png")));
                                imagePath = "/assets/data/hydraulicUnitData/schematicImages/tek_hiz_kompanzasyon_arti_tek_hiz_black.png";
                                reverseImagePath = "/assets/data/hydraulicUnitData/schematicImages/tek_hiz_kompanzasyon_arti_tek_hiz_black.png";
                                imageTextEnable(calculatedX, calculatedY, "tek_hiz_kompanzasyon_arti_tek_hiz");
                            }
                        }
                    }
                }

                tankGorselLoad();

                schemeImage.setImage(image);

                hesaplamaBitti = true;
                
                // Ünite bilgileri bölümünü kapat
                if(isUnitInfoSectionExpanded) {
                    collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false, true);
                    isUnitInfoSectionExpanded = false;
                }
                
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
            } else {
                NotificationUtil.showNotification(classicCaclulationTitle.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Hesaplama Hatası", "Hesaplama sonucu beklenmeyen bir hata oluştu.");
            }
        }
    }

    private void enableSonucSection() {
        collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, true, false);
    }

    private void tankGorselLoad() {
        Image image;

        if(secilenSogutmaDurumu.equals("Var")) {
            if(secilenHidrolikKilitDurumu.equals("Var")) {
                if(secilenValfTipi.equals("İnişte Çift Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/sogutma_kilit_cift_hiz.png")));
                    tankImage.setImage(image);
                } else if(secilenValfTipi.contains("İnişte Tek Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/sogutma_kilit_tek_hiz.png")));
                    tankImage.setImage(image);
                }
            } else {
                if(secilenValfTipi.equals("İnişte Çift Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/sogutma_cift_hiz.png")));
                    tankImage.setImage(image);
                } else if(secilenValfTipi.contains("İnişte Tek Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/sogutma_tek_hiz.png")));
                    tankImage.setImage(image);
                }
            }
        } else {
            if(secilenHidrolikKilitDurumu.equals("Var")) {
                if(secilenValfTipi.equals("İnişte Çift Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/kilit_ayri_cift_hiz.png")));
                    tankImage.setImage(image);
                } else if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/kilit_ayri_kompanzasyon.png")));
                    tankImage.setImage(image);
                } else if(secilenValfTipi.equals("İnişte Tek Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/kilit_ayri_tek_hiz.png")));
                    tankImage.setImage(image);
                } else if(secilenValfTipi.equals("Kilitli Blok")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/kilitli_blok.png")));
                    tankImage.setImage(image);
                }
            } else {
                if(secilenValfTipi.equals("İnişte Çift Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/cift_hiz.png")));
                    tankImage.setImage(image);
                } else if(secilenValfTipi.equals("İnişte Tek Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/tek_hiz.png")));
                    tankImage.setImage(image);
                } else if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                    image = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/data/hydraulicUnitData/cabins/classic/tek_hiz_kompanzasyon.png")));
                    tankImage.setImage(image);
                }
            }
        }
    }

    private void dataInit(String componentName, @Nullable Integer valfTipiStat) {
        if(componentName.equals("motor")) {
            motorComboBox.setDisable(false);
            motorComboBox.getItems().clear();
            motorComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().motorMap.get("0"));
        } else if(componentName.equals("pompa")) {
            pompaComboBox.setDisable(false);
            pompaComboBox.getItems().clear();
            if(Objects.equals(secilenUniteTipi, "Hidros")) {
                pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().pumpMap.get("0"));
            } else if(Objects.equals(secilenUniteTipi, "Klasik")) {
                pompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().pumpMap.get("1"));
            } else {
                LinkedList<String> list1 = SystemDefaults.getLocalHydraulicData().pumpMap.get("0");
                LinkedList<String> list2 = SystemDefaults.getLocalHydraulicData().pumpMap.get("1");
                LinkedList<String> combinedList = new LinkedList<>();
                combinedList.addAll(list1);
                combinedList.addAll(list2);

                pompaComboBox.getItems().addAll(combinedList);
            }
        } else if(componentName.equals("valfTipi")) {
            valfTipiComboBox.setDisable(false);
            valfTipiComboBox.getItems().clear();
            if(valfTipiStat == 1) {
                valfTipiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().valveTypeMap.get("0")); //İnişte Tek Hız, İnişte Çift Hız
            } else if(valfTipiStat == 0) {
                valfTipiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().valveTypeMap.get("1")); //Kompanzasyon || İnişte Tek Hız
            } else {
                valfTipiComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().valveTypeMap.get("2")); //İnişte Çift Hız
            }
        } else if(componentName.equals("hidrolikKilit")) {
            hidrolikKilitComboBox.setDisable(false);
            hidrolikKilitComboBox.getItems().clear();
            hidrolikKilitComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().hydraulicLockMap.get("0"));
        } else if(componentName.equals("sogutma")) {
            sogutmaComboBox.setDisable(false);
            sogutmaComboBox.getItems().clear();
            sogutmaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().coolingMap.get("0"));
        } else if(componentName.equals("kilitMotor")) {
            kilitMotorComboBox.setDisable(false);
            kilitMotorComboBox.getItems().clear();
            kilitMotorComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().lockMotorMap.get("0"));
        } else if(componentName.equals("yagMiktari")) {
            gerekenYagMiktariField.setDisable(false);
        } else if(componentName.equals("kilitPompa")) {
            kilitPompaComboBox.setDisable(false);
            kilitPompaComboBox.getItems().clear();
            kilitPompaComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().lockPumpMap.get("0"));
        } else if(componentName.equals("kompanzasyon")) {
            kompanzasyonComboBox.setDisable(false);
            kompanzasyonComboBox.getItems().clear();
            kompanzasyonComboBox.getItems().addAll(SystemDefaults.getLocalHydraulicData().compensationMap.get("0"));
        }
    }

    private void initValfValues() {
        valfTipiComboBox.getItems().clear();

        if(secilenSogutmaDurumu.equals("Var")) {
            if(secilenHidrolikKilitDurumu.equals("Var")) {
                if(kompanzasyonDurumu.equals("Var")) {
                    dataInit("valfTipi", 0); //Komp + Tek
                } else {
                    dataInit("valfTipi", 2); //Sadece Çift
                }
            } else {
                if(kompanzasyonDurumu.equals("Var")) {
                    dataInit("valfTipi", 0); //Komp + Tek
                } else {
                    dataInit("valfTipi", 2); //Sadece Çift
                }
            }
        } else {
            if(secilenHidrolikKilitDurumu.equals("Var")) {
                if(kompanzasyonDurumu.equals("Var")) {
                    dataInit("valfTipi", 0);
                } else {
                    if(secilenPompaVal <= 28.1) {
                        valfTipiComboBox.getItems().clear();
                        valfTipiComboBox.getItems().addAll("Kilitli Blok");
                        valfTipiComboBox.setDisable(false);
                    } else {
                        dataInit("valfTipi", 1); //Tek + Çift
                    }
                }
            } else {
                if(kompanzasyonDurumu.equals("Var")) {
                    dataInit("valfTipi", 0); //Komp + Tek
                } else {
                    dataInit("valfTipi", 1); //Tek + Çift
                }
            }
        }

        disableKilitMotorAndPompa();
    }

    private void disableKilitMotorAndPompa() {
        kilitMotorComboBox.setDisable(true);
        if(secilenKilitMotor != null) {
            kilitMotorComboBox.getSelectionModel().clearSelection();
            secilenKilitMotor = null;
        }

        kilitPompaComboBox.setDisable(true);
        if(secilenKilitPompa != null) {
            kilitPompaComboBox.getSelectionModel().clearSelection();
            secilenKilitPompa = null;
        }
    }

    private void imageTextDisable() {
        for(Text text : sonucTexts) {
            hydraulicUnitSchemePane.getChildren().remove(text);
        }
        sonucTexts.clear();
    }

    private void imageTextEnable(int x, int y, String calculatedImage) {
        sonucTexts.clear();

        if(calculatedImage.equals("cift_hiz")) {
            addTextToList("X: " + x + " mm", 196, 75, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -15, 230, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("0")), 135, 105, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("1")), 35, 175, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("2")), 30, 310, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("3")), 70, 320, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("4")), 100, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("5")), 65, 265, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("6")), 185, 270, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("6")));
            addTextToList(secilenValfTipi, 100, 195, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 280, 180, 0, 12, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("7")), 330, 115, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("8")), 405, 175, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("9")), 375, 290, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("10")), 365, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("11")), 415, 285, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("11")));
        } else if (calculatedImage.equals("kilit_ayri_cift_hiz")) {
            addTextToList("X: " + x + " mm", 210, 70, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("0")), 135, 85, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("1")), 50, 120, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("2")), 120, 125, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("2")));
            addTextToList(getKampanaText(), 300, 150, 0, 10.5, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("3")), 350, 90, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("4")), 400, 170, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("5")), 120, 220, -90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("5")));
            addTextToList(secilenValfTipi, 90, 295, 0, 10, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("6")), 40, 315, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("7")), 175, 320, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("8")), 220, 290, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("9")), 295, 290, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("10")), 125, 330, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("11")), 215, 335, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("12")), 285, 408, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("13")), 355, 295, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("14")), 355, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("15")), 410, 285, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("15")));
        } else if(calculatedImage.equals("kilit_ayri_tek_hiz")) {
            addTextToList("X: " + x + " mm", 210, 70, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("0")), 135, 85, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("1")), 50, 120, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("2")), 120, 125, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("2")));
            addTextToList(getKampanaText(), 305, 150, 0, 10.5, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("3")), 350, 90, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("4")), 405, 170, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("5")), 110, 200, -90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("6")), 50, 295, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("6")));
            addTextToList(secilenValfTipi, 145, 265, 0, 10, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("7")), 360, 330, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("8")), 215, 335, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("9")), 155, 310, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("10")), 240, 310, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("11")), 215, 285, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("12")), 295, 285, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("13")), 360, 295, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("14")), 120, 335, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("15")), 410, 285, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("15")));
        } else if(calculatedImage.equals("kilitli_blok")) {
            addTextToList("X: " + x + " mm", 210, 75, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("0")), 120, 105, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("1")), 40, 210, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("2")), 90, 290, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("3")), 195, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("4")), 205, 285, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("4")));
            addTextToList(secilenValfTipi, 95, 195, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 290, 185, 0, 12, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("5")), 335, 110, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("6")), 405, 180, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("7")), 385, 290, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("8")), 370, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("9")), 420, 285, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("9")));
        } else if(calculatedImage.equals("tek_hiz_kompanzasyon_arti_tek_hiz")) {
            addTextToList("X: " + x + " mm", 210, 75, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("0")), 135, 105, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("1")), 45, 210, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("2")), 30, 305, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("3")), 100, 275, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("4")), 65, 320, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("5")), 65, 270, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("6")), 205, 270, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("6")));
            addTextToList(secilenValfTipi, 75, 230, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 285, 185, 0, 12, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("7")), 330, 105, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("8")), 405, 180, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("9")), 375, 300, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("10")), 370, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("11")), 410, 285, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("11")));
        } else if(calculatedImage.equals("sogutma_kilitsiz_cift_hiz")) {
            addTextToList("X: " + x + " mm", 210, 80, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("0")), 65, 225, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("1")), 25, 280, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("2")), 90, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("3")), 125, 290, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("4")), 140, 215, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("5")), 90, 170, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("6")), 290, 220, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("7")), 145, 140, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("8")), 145, 190, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("9")), 210, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("10")), 285, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("11")), 375, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("12")), 420, 280, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("13")), 270, 265, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("14")), 150, 235, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("15")), 295, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("16")), 355, 185, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("17")), 235, 130, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("18")), 260, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("19")), 365, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("20")), 350, 95, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("21")), 190, 105, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("22")), 390, 105, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("23")), 420, 120, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("24")), 385, 140, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("24")));
            addTextToList(secilenValfTipi, 60, 270, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 340, 255, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("25")), 175, 265, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("25")));
        } else if(calculatedImage.equals("sogutma_kilitli_cift_hiz")) {
            addTextToList("X: " + x + " mm", 210, 80, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("0")), 65, 110, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("1")), 25, 170, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("2")), 65, 225, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("3")), 25, 280, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("4")), 90, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("5")), 125, 290, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("6")), 140, 215, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("7")), 90, 170, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("8")), 150, 235, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("9")), 145, 140, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("10")), 145, 190, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("11")), 375, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("12")), 285, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("13")), 210, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("14")), 420, 285, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("15")), 270, 265, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("16")), 290, 220, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("17")), 295, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("18")), 355, 185, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("19")), 235, 130, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("20")), 260, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("21")), 365, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("22")), 350, 95, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("23")), 190, 105, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("24")), 390, 110, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("24")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("25")), 420, 120, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("25")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("26")), 385, 140, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("26")));
            addTextToList(secilenValfTipi, 60, 270, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 335, 255, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("27")), 170, 265, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("27")));
        } else if(calculatedImage.equals("sogutma_kilitsiz_tek_hiz")) {
            addTextToList("X: " + x + " mm", 210, 80, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("0")), 65, 225, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("1")), 35, 290, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("2")), 90, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("3")), 125, 290, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("4")), 140, 215, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("5")), 90, 170, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("6")), 150, 235, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("7")), 145, 190, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("8")), 145, 140, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("9")), 210, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("10")), 285, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("11")), 375, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("12")), 420, 280, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("13")), 270, 265, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("14")), 290, 220, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("15")), 295, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("16")), 355, 185, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("17")), 235, 130, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("18")), 260, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("19")), 365, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("20")), 350, 95, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("21")), 190, 105, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("22")), 390, 110, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("23")), 420, 120, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("24")), 385, 140, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("24")));
            addTextToList(secilenValfTipi, 30, 255, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 335, 255, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("25")), 175, 265, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("25")));
        } else if (calculatedImage.equals("sogutma_kilitli_tek_hiz")) {
            addTextToList("X: " + x + " mm", 210, 80, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", -12, 220, -90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("0")), 70, 115, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("1")), 30, 170, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("2")), 65, 225, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("3")), 35, 295, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("4")), 90, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("5")), 125, 290, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("6")), 140, 215, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("7")), 90, 170, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("8")), 150, 235, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("9")), 145, 190, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("10")), 145, 140, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("11")), 375, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("12")), 285, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("13")), 210, 325, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("14")), 420, 285, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("15")), 270, 265, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("16")), 290, 220, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("17")), 295, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("18")), 355, 185, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("19")), 235, 130, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("20")), 260, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("21")), 365, 165, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("22")), 350, 95, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("23")), 190, 105, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("24")), 390, 110, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("24")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("25")), 420, 120, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("25")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("26")), 385, 140, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("26")));
            addTextToList(secilenValfTipi, 30, 255, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 335, 255, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("27")), 175, 265, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("27")));
        }

        for (Text text : sonucTexts) {
            hydraulicUnitSchemePane.getChildren().add(text);
        }
    }

    private void tabloGuncelle() {
        sonucTablo.getItems().clear();
        DataControlTable data = new DataControlTable("Sipariş Numarası:", girilenSiparisNumarasi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Hidrolik Ünitesi Tipi:", secilenUniteTipi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Seçilen Motor:", secilenMotor);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Kampana:", String.valueOf(secilenKampana));
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Soğutma Durumu:", secilenSogutmaDurumu);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Hidrolik Kilit Durumu:", secilenHidrolikKilitDurumu);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Seçilen Pompa:", secilenPompa);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Tank Kapasitesi:", String.valueOf(girilenTankKapasitesiMiktari));
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Kompanzasyon:", String.valueOf(kompanzasyonDurumu));
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Seçilen Valf Tipi:", secilenValfTipi);
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Kilit Motoru:", Objects.requireNonNullElse(secilenKilitMotor, "Yok"));
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Kilit Pompa:", Objects.requireNonNullElse(secilenKilitPompa, "Yok"));
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Hesaplanan X", String.valueOf(calculationResultX));
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Hesaplanan Y", String.valueOf(calculationResultY));
        sonucTablo.getItems().add(data);

        data = new DataControlTable("Hesaplanan H", String.valueOf(calculationResultH));
        sonucTablo.getItems().add(data);
    }

    private void addTextToList(String content, int x, int y, int rotate, double fontSize, Color color, String tooltipText) {
        Text text = new Text(content);
        text.setX(x);
        text.setY(y);
        text.setRotate(rotate);
        text.setFill(color);
        text.setFont(new Font(fontSize));
        text.setTextAlignment(TextAlignment.CENTER);

        if(tooltipText != null) {
            tooltipText = "Key: " + tooltipText + "\n \nÜstteki anahtarı schematic_texts.yml'de aratarak değeri güncelleyebilirsiniz.";
        } else {
            tooltipText = "Hesaplama sonucu üretilen değerdir.";
        }

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.seconds(0.1));
        tooltip.setStyle("-fx-font-size: 14px;");
        Tooltip.install(text, tooltip);

        text.setOnMouseEntered(e -> {
            text.setUnderline(true);
        });

        text.setOnMouseExited(e -> {
            text.setUnderline(false);
        });

        sonucTexts.add(text);
    }

    private String getKampanaText() {
        String kampanaText = "";

        if(secilenPompaVal >= 33.3) {
            if(secilenKampana == 250) {
                kampanaText = "Kampana: 2K-" + secilenKampana + "\nKesim Çapı: Ø" + 173;
            } else if(secilenKampana == 300) {
                kampanaText = "Kampana: 2K-" + secilenKampana + "\nKesim Çapı: Ø" + 236;
            } else if(secilenKampana == 350) {
                kampanaText = "Kampana: 2K-" + secilenKampana + "\nKesim Çapı: Ø" + 263;
            } else if(secilenKampana == 400) {
                kampanaText = "Kampana: 2K-" + secilenKampana + "\nKesim Çapı: Ø" + " NaN";
            }
        } else {
            if(secilenKampana == 250) {
                kampanaText = "Kampana: " + secilenKampana + "\nKesim Çapı: Ø" + 173;
            } else if(secilenKampana == 300) {
                kampanaText = "Kampana: " + secilenKampana + "\nKesim Çapı: Ø" + 236;
            } else if(secilenKampana == 350) {
                kampanaText = "Kampana: " + secilenKampana + "\nKesim Çapı: Ø" + 263;
            } else if(secilenKampana == 400) {
                kampanaText = "Kampana: " + secilenKampana + "\nKesim Çapı: Ø" + " NaN";
            }
        }

        return kampanaText;
    }

    ArrayList<Integer> calcDimensions() {
        //Eklenecek Değerler
        int kampanaDegeri = Integer.parseInt(SystemDefaults.getLocalHydraulicData().motorKampanaMap.get(motorComboBox.getSelectionModel().getSelectedItem().toString()).replace(" mm", ""));

        secilenKampana = Integer.parseInt(SystemDefaults.getLocalHydraulicData().motorKampanaMap.get(motorComboBox.getSelectionModel().getSelectedItem().toString()).replace(" mm", ""));
        secilenPompaVal = Utils.stringToDouble(secilenPompa);

        //Hesaplama Standartları
        ArrayList<Integer> finalValues = new ArrayList<>();
        int yV = 0;
        int yK = 0;
        int eskiX=0, eskiY=0, eskiH=0;
        int x=0, y=0, h=0;

        int atananHacim=0;

        System.out.println("--------Hesaplama Başladı--------");
        if(Objects.equals(secilenSogutmaDurumu, "Var")) {
            /* TODO
            Standart üniteyi göster. Ölçüler:
            X: 1000
            Y: 600
            H: 350
            Soğutmanın standardı için bir kabin eklenecek :)
             */

            atananHacim = 200;
            calculationSogutma(x, y, h, finalValues, atananKabinFinal, gecisOlculeriFinal, atananHacim);

            return finalValues;
        } else {
            x +=  kampanaDegeri;
            yK += kampanaDegeri;
            System.out.println("Başlangıç:");
            System.out.println("X: " + x + " yK: " + yK + " yV: " + yV);
            System.out.println("Motor + Kampana için:");
            System.out.println("X += " + kampanaDegeri + " (Kampana)");
            System.out.println("yK += " + kampanaDegeri + " (Kampana)");

            if(secilenHidrolikKilitDurumu.equals("Var")) {
                //Hidrolik Kilit Var
                if(secilenKilitMotor != null) {
                    //Kilit Motor Var
                    if(kompanzasyonDurumu.equals("Var")) {
                        //Kompanzasyon Var
                        if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                            x += SystemDefaults.getLocalHydraulicData().kilitMotorTankArasiBoslukX + SystemDefaults.getLocalHydraulicData().kilitMotorX + SystemDefaults.getLocalHydraulicData().kilitMotorAraBoslukX + SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukX;
                            yV += SystemDefaults.getLocalHydraulicData().kilitMotorTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kilitMotorY + SystemDefaults.getLocalHydraulicData().kilitMotorYOn + SystemDefaults.getLocalHydraulicData().tekHizKilitAyriY + SystemDefaults.getLocalHydraulicData().tekHizBlokY + SystemDefaults.getLocalHydraulicData().tekHizKilitAyriYOn;
                            yK += SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kampanaBoslukYOn;
                        }
                    } else {
                        //Kompanzasyon Yok
                        if(secilenValfTipi.equals("İnişte Tek Hız")) {
                            x += SystemDefaults.getLocalHydraulicData().kilitMotorTankArasiBoslukX + SystemDefaults.getLocalHydraulicData().kilitMotorX + SystemDefaults.getLocalHydraulicData().kilitMotorAraBoslukX + SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukX;
                            yV += SystemDefaults.getLocalHydraulicData().kilitMotorTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kilitMotorY + SystemDefaults.getLocalHydraulicData().kilitMotorYOn + SystemDefaults.getLocalHydraulicData().tekHizKilitAyriY + SystemDefaults.getLocalHydraulicData().tekHizBlokY + SystemDefaults.getLocalHydraulicData().tekHizKilitAyriYOn;
                            yK += SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kampanaBoslukYOn;
                        } else if(secilenValfTipi.equals("İnişte Çift Hız")) {
                            x += SystemDefaults.getLocalHydraulicData().kilitMotorTankArasiBoslukX + SystemDefaults.getLocalHydraulicData().kilitMotorX + SystemDefaults.getLocalHydraulicData().kilitMotorAraBoslukX + SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukX;
                            yV += SystemDefaults.getLocalHydraulicData().kilitMotorTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kilitMotorY + SystemDefaults.getLocalHydraulicData().kilitMotorYOn + SystemDefaults.getLocalHydraulicData().ciftHizKilitAyriY + SystemDefaults.getLocalHydraulicData().ciftHizBlokY + SystemDefaults.getLocalHydraulicData().ciftHizKilitAyriYOn;
                            yK += SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kampanaBoslukYOn;
                        }
                    }
                } else {
                    //Kilit Motor Yok
                    if(secilenValfTipi.equals("Kilitli Blok")) {
                        x += SystemDefaults.getLocalHydraulicData().kilitliBlokTankArasiBoslukX + SystemDefaults.getLocalHydraulicData().kilitliBlokX + SystemDefaults.getLocalHydraulicData().kilitliBlokAraBoslukX + SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukX;
                        yV += SystemDefaults.getLocalHydraulicData().kilitliBlokTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kilitliBlokY + SystemDefaults.getLocalHydraulicData().kilitliBlokYOn;
                        yK += SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kampanaBoslukYOn;
                    }
                }
            } else {
                //Hidrolik Kilit Yok
                if(kompanzasyonDurumu.equals("Var")) {
                    //kompanzasyon Var
                    if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                        x += SystemDefaults.getLocalHydraulicData().tekHizTankArasiBoslukX + SystemDefaults.getLocalHydraulicData().tekHizBlokX + SystemDefaults.getLocalHydraulicData().tekHizAraBoslukX + SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukX;
                        yV += SystemDefaults.getLocalHydraulicData().tekHizTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().tekHizBlokY + SystemDefaults.getLocalHydraulicData().tekHizYOn;
                        yK += SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kampanaBoslukYOn;
                    }
                } else {
                    //Kompanzasyon Yok
                    if(secilenValfTipi.equals("İnişte Tek Hız")) {
                        x += SystemDefaults.getLocalHydraulicData().tekHizTankArasiBoslukX + SystemDefaults.getLocalHydraulicData().tekHizBlokX + SystemDefaults.getLocalHydraulicData().tekHizAraBoslukX + SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukX;
                        yV += SystemDefaults.getLocalHydraulicData().tekHizTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().tekHizBlokY + SystemDefaults.getLocalHydraulicData().tekHizYOn;
                        yK += SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kampanaBoslukYOn;
                    } else if(secilenValfTipi.equals("İnişte Çift Hız")) {
                        x += SystemDefaults.getLocalHydraulicData().ciftHizTankArasiBoslukX + SystemDefaults.getLocalHydraulicData().ciftHizBlokX + SystemDefaults.getLocalHydraulicData().ciftHizAraBoslukX + SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukX;
                        yV += SystemDefaults.getLocalHydraulicData().ciftHizTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().ciftHizBlokY + SystemDefaults.getLocalHydraulicData().ciftHizYOn;
                        yK += SystemDefaults.getLocalHydraulicData().kampanaTankArasiBoslukY + SystemDefaults.getLocalHydraulicData().kampanaBoslukYOn;
                    }
                }
            }

            y = Math.max(yV, yK);
            if(y <= 350) {
                y = 350;
            }
            if(x <= 550) {
                x = 550;
            }
            h = SystemDefaults.getLocalHydraulicData().defaultHeight;

            String veri = SystemDefaults.getLocalHydraulicData().motorYukseklikMap.get(motorComboBox.getSelectionModel().getSelectedItem());
            String sayiKismi = veri.replaceAll("[^0-9]", "");
            motorYukseklik = Integer.parseInt(sayiKismi);

            eskiX = x;
            eskiY = y;
            eskiH = h;
        }

        calculationResultX = x;
        calculationResultY = y;
        calculationResultH = h;
        tabloGuncelle();

        Kabin finalTank = null;
        for(Kabin selectedTank : SystemDefaults.getLocalHydraulicData().classicCabins) {
            int selectedTankKabinHacim = selectedTank.getKabinHacim();
            int selectedTankKabinX = selectedTank.getTankDisX();
            int selectedTankKabinY = selectedTank.getTankDisY();
            int selectedTankKabinH = selectedTank.getTankDisH();

            if(selectedTankKabinX >= x && selectedTankKabinY >= y && selectedTankKabinH >= h) {
                int kayipLitre = SystemDefaults.getLocalHydraulicData().kayipLitre;
                int hacimMinVal = selectedTankKabinHacim - kayipLitre;
                int hacimMaxVal = selectedTankKabinHacim + kayipLitre;
                System.out.println("Girilen Deger: " + girilenTankKapasitesiMiktari
                        + "Min Val: " + hacimMinVal
                        + " Max Val: " + hacimMaxVal
                        + " Secilen Deger: " + selectedTankKabinHacim);
                if (girilenTankKapasitesiMiktari >= hacimMinVal && girilenTankKapasitesiMiktari <= hacimMaxVal) {
                    finalTank = selectedTank;
                    System.out.println("Tank seçildi: " + selectedTankKabinHacim);
                    break;
                } else if(selectedTankKabinHacim > girilenTankKapasitesiMiktari) {
                    finalTank = selectedTank;
                    System.out.println("Tank seçildi: " + selectedTankKabinHacim);
                    break;
                } else {
                    System.out.println("Girilen değer aralığa uymuyor.");
                }
            }
        }

        if (finalTank != null) {
            x = finalTank.getTankDisX();
            y = finalTank.getTankDisY();
            h = finalTank.getTankDisH();
            atananHacim = finalTank.getKabinHacim();
            atananHT = finalTank.getTankName();
            if(finalTank.getKabinGecisH() < (motorYukseklik + h)) {
                for(Kabin selectedTank : SystemDefaults.getLocalHydraulicData().classicCabins) {
                    int kabinYukseklik = selectedTank.getKabinGecisH();
                    //System.out.println("Kabin Yükseklik: " + kabinYukseklik + "\nÖnceden Seçilen Kabin Yükseklik: " + finalTank.getKabinH());

                    int tempYukseklik = finalTank.getKabinGecisH() + motorYukseklik;
                    System.out.println("Yukseklik Hesaplama: " + tempYukseklik + "   Kabin Yükseklik: " + kabinYukseklik);

                    if(kabinYukseklik >= tempYukseklik) {
                        finalTank = selectedTank;
                        System.out.println("Yeni Kabin: " + finalTank.getKabinName());
                        break;
                    }
                }
            }
        }

        String atananKabin = finalTank.getKabinName();
        String disOlculer = finalTank.getKabinOlculeri();
        String gecisOlculeri = finalTank.getGecisOlculeri();
        String tankOlculeri = finalTank.getTankOlculeri();

        //kullanilacakKabin.setText("Kullanmanız Gereken Kabin: \n\t\t\t\t\t\t" + atananKabin + "\n\t\t\tGeçiş Ölçüleri: " + gecisOlculeri + " (x, y, h)");
        kabinTitle.setText("Kabin: " + atananKabin);
        disOlculerLabel.setText("Dış Ölçüler: " + disOlculer + " (x, y, h)");
        gecisOlculeriLabel.setText("Geçiş Ölçüleri: " + gecisOlculeri + " (x, y, h)");
        tankOlculeriLabel.setText("Tank Ölçüleri: " + tankOlculeri + " (x, y, h)");
        atananKabinFinal = atananKabin;
        gecisOlculeriFinal = gecisOlculeri;
        //int secilenMotorIndeks = motorComboBox.getSelectionModel().getSelectedIndex();
        //int motorYukseklikDegeri = Integer.parseInt(motorYukseklikVerileri.get(secilenMotorIndeks));

        logCalculation(yV, yK, eskiX, eskiY, eskiH, x, y, h, atananHacim, atananKabin, gecisOlculeri);

        finalValues.add(x);
        finalValues.add(y);
        finalValues.add(h);
        finalValues.add(atananHacim);
        return finalValues;
    }

    private void calculationSogutma(int x, int y, int h, ArrayList<Integer> finalValues, String atananKabinFinal, String gecisOlculeriFinal, int atananHacim) {
        x = 1000;
        y = 600;
        h = 350;

        atananHT = "HT SOĞUTMA";
        String atananKabin = "KD SOĞUTMA";
        String gecisOlculeri = "1000x600x350";

        kabinTitle.setText("Kabin: " + atananKabin);
        disOlculerLabel.setText("Dış Ölçüler: " + "Veri YOK" + " (x, y, h)");
        gecisOlculeriLabel.setText("Geçiş Ölçüleri: " + gecisOlculeri + " (x, y, h)");
        tankOlculeriLabel.setText("Tank Ölçüleri: " + "Veri YOK" + " (x, y, h)");

        System.out.println("--------Hesaplama Bitti--------");
        System.out.println("------------(Sonuç)------------");
        System.out.println("Atanan X: " + x);
        System.out.println("Atanan Y: " + y);
        System.out.println("Atanan h: " + h);
        System.out.println("Atanan Hacim: " + atananHacim);
        System.out.println("Kullanmanız Gereken Kabin: " + atananKabin);
        System.out.println("Geçiş Ölçüleri: " + gecisOlculeri);
        System.out.println("-------------------------------");

        finalValues.add(x);
        finalValues.add(y);
        finalValues.add(h);
        finalValues.add(atananHacim);
    }

    private void logCalculation(int yV, int yK, int eskiX, int eskiY, int eskiH, int x, int y, int h, int atananHacim, String atananKabin, String gecisOlculeri) {
        System.out.println("--------Hesaplama Bitti--------");
        System.out.println("------------(Sonuç)------------");
        System.out.println("yV: " + yV);
        System.out.println("yK: " + yK);
        System.out.println("Hesaplanan X: " + eskiX);
        System.out.println("Hesaplanan Y: " + eskiY);
        System.out.println("Hesaplanan h: " + eskiH);
        System.out.println("Atanan X: " + x);
        System.out.println("Atanan Y: " + y);
        System.out.println("Atanan h: " + h);
        System.out.println("Atanan Hacim: " + atananHacim);
        System.out.println("Kullanmanız Gereken Kabin: " + atananKabin);
        System.out.println("Geçiş Ölçüleri: " + gecisOlculeri);
        System.out.println("-------------------------------");
    }

    public void exportSchemeProcess() {
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

            PDFUtil.pdfGenerator("/assets/images/logos/onderlift-logo.png", tankImagePaneSection, hydraulicUnitSchemePane, "/assets/data/hydraulicUnitData/schematicPDF/classic/" + pdfPath, girilenSiparisNumarasi, kabinTitle.getText().toString(), secilenMotor, secilenPompa, secilenUniteTipi, true);
            // PDF üretildikten sonra "Dosyada Göster" butonunu görünür yap
            if(openPDFInExplorerButton != null) {
                openPDFInExplorerButton.setVisible(true);
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
        boolean isSogutmaVar = secilenSogutmaDurumu.equals("Var");
        boolean isKilitVar = secilenHidrolikKilitDurumu != null;
        boolean isKompanzasyonVar = kompanzasyonDurumu.equals("Var");
        boolean isKilitMotorVar = secilenKilitMotor != null;

        if(isSogutmaVar) {
            if(isKilitVar && isKilitMotorVar) {
                if(isKompanzasyonVar) {
                    if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 13, 14, 15, 16);
                    }
                } else {
                    if(secilenValfTipi.equals("İnişte Çift Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 5, 6, 7, 8);
                    }
                }
            } else {
                if(isKompanzasyonVar) {
                    if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 9, 10, 11, 12);
                    }
                } else {
                    if(secilenValfTipi.equals("İnişte Çift Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 1, 2, 3, 4);
                    }
                }
            }
        } else {
            if(isKilitVar && isKilitMotorVar) {
                if(isKompanzasyonVar) {
                    if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 33, 34, 35, 36);
                    }
                } else {
                    if(secilenValfTipi.equals("İnişte Tek Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 17, 18, 19, 20);
                    } else if(secilenValfTipi.equals("İnişte Çift Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 21, 22, 23, 24);
                    }
                }
            } else {
                if(isKompanzasyonVar) {
                    if(secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 29, 30, 31, 32);
                    }
                } else {
                    if(secilenValfTipi.equals("İnişte Tek Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 41, 42, 43, 44);
                    } else if(secilenValfTipi.equals("İnişte Çift Hız")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 37, 38, 39, 40);
                    } else if(secilenValfTipi.equals("Kilitli Blok")) {
                        return getCylinderImage(selectedCylinders, isPressureValf, 25, 26, 27, 28);
                    }
                }
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

    private void createAndLoadPartListTable() {
        if(secilenSogutmaDurumu.equals("Yok")) {
            loadStockCodes();
        }

        loadMotorParca();
        loadKampanaParca();
        loadPompaParca();
        loadKaplinParca();
        loadValfBlokParca();

        if(ClassicController.secilenKilitMotor != null) {
            loadKilitMotorParca();
        }

        loadStandartParca();
        if(secilenSogutmaDurumu.contains("Var")) {
            loadSogutucuParca();
        }

        if(basincSalteriDurumu.equals("Var")) {
            loadBasincSalteriParca();
        }

        if(elPompasiDurumu.equals("Var")) {
            loadElPompasiParca();
        }

        loadYagMiktari();
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

        partListTable.setRowFactory(tv -> new TableRow<PartListTable>() {
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

    private void loadStockCodes() {
        String adet = "1";

        Kabin foundedTank = Utils.findClassicTankByKabinName(ClassicController.atananKabinFinal);
        List<PartListTable> dataList = Arrays.asList(
                new PartListTable("----", "Kabin Genel Bilgisi", "----"),
                new PartListTable(foundedTank.getKabinKodu(), foundedTank.getMalzemeAdi(), adet),
                new PartListTable(foundedTank.getYagTankiKodu(), foundedTank.getTankName(), adet)
        );

        partListTable.getItems().addAll(dataList);
    }

    private void loadKampanaParca() {
        if(ClassicController.secilenPompaVal >= 33.3) {
            if(ClassicController.secilenKampana == 250) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("4"), "Kampana Parçaları");
            } else if(ClassicController.secilenKampana == 300) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("5"), "Kampana Parçaları");
            } else if(ClassicController.secilenKampana == 350) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("6"), "Kampana Parçaları");
            } else if(ClassicController.secilenKampana == 400) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("7"), "Kampana Parçaları");
            }
        } else {
            if(ClassicController.secilenKampana == 250) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("0"), "Kampana Parçaları");
            } else if(ClassicController.secilenKampana == 300) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("1"), "Kampana Parçaları");
            } else if(ClassicController.secilenKampana == 350) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("2"), "Kampana Parçaları");
            } else if(ClassicController.secilenKampana == 400) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKampana.get("3"), "Kampana Parçaları");
            }
        }
    }

    private void loadPompaParca() {
        if(Objects.equals(secilenPompa, "9.5 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("0"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "11.9 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("1"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "14 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("2"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "14.6 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("3"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "16.8 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("4"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "19.2 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("5"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "22.9 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("6"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "28.1 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("7"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "28.8 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("8"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "33.3 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("9"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "37.9 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("10"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "42.6 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("11"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "45.5 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("12"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "49.4 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("13"), "Pompa Parçaları");
        } else if(Objects.equals(secilenPompa, "56.1 cc")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaPompa.get("14"), "Pompa Parçaları");
        }
    }

    private void loadMotorParca() {
        if(Objects.equals(secilenMotor, "2.2 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("0"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "3 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("1"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "4 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("2"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "5.5 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("3"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "5.5 kW (Kompakt)")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("4"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "7.5 kW (Kompakt)")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("5"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "11 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("6"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "11 kW (Kompakt)")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("7"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "15 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("8"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "18.5 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("9"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "22 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("10"), "Motor Parçaları");
        } else if(Objects.equals(secilenMotor, "37 kW")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaMotor.get("11"), "Motor Parçaları");
        }
    }

    private void loadKaplinParca() {
        String[] secPmp = ClassicController.secilenPompa.split(" cc");
        float secilenPompaVal = Float.parseFloat(secPmp[0]);

        if(secilenPompaVal < 33.3) {
            if(Objects.equals(secilenMotor, "2.2 kW") || Objects.equals(secilenMotor, "3 kW") || Objects.equals(secilenMotor, "4 kW") || Objects.equals(secilenMotor, "5.5 kW") || Objects.equals(secilenMotor, "5.5 kW (Kompakt)")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKaplin.get("0"), "Kaplin Parçaları");
            } else if(Objects.equals(secilenMotor, "7.5 kW (Kompakt)") || Objects.equals(secilenMotor, "11 kW") || Objects.equals(secilenMotor, "11 kW (Kompakt)")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKaplin.get("1"), "Kaplin Parçaları");
            } else if(Objects.equals(secilenMotor, "15 kW") || Objects.equals(secilenMotor, "18.5 kW") || Objects.equals(secilenMotor, "22 kW") || Objects.equals(secilenMotor, "37 kW")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKaplin.get("2"), "Kaplin Parçaları");
            }
        } else {
            if(Objects.equals(secilenMotor, "2.2 kW") || Objects.equals(secilenMotor, "3 kW") || Objects.equals(secilenMotor, "4 kW") || Objects.equals(secilenMotor, "5.5 kW") || Objects.equals(secilenMotor, "5.5 kW (Kompakt)")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKaplin.get("3"), "Kaplin Parçaları");
            } else if(Objects.equals(secilenMotor, "7.5 kW (Kompakt)") || Objects.equals(secilenMotor, "11 kW") || Objects.equals(secilenMotor, "11 kW (Kompakt)")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKaplin.get("4"), "Kaplin Parçaları");
            } else if(Objects.equals(secilenMotor, "15 kW") || Objects.equals(secilenMotor, "18.5 kW") || Objects.equals(secilenMotor, "22 kW") || Objects.equals(secilenMotor, "37 kW")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKaplin.get("5"), "Kaplin Parçaları");
            }
        }
    }

    private void loadValfBlokParca() {
        if(ClassicController.secilenPompaVal < 33.3) {
            //1 Grubu
            if(Objects.equals(secilenValfTipi, "İnişte Tek Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("0"), "Valf Blok Parçaları");
            } else if(Objects.equals(secilenValfTipi, "İnişte Çift Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("1"), "Valf Blok Parçaları");
            } else if(Objects.equals(secilenValfTipi, "Kilitli Blok")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("2"), "Valf Blok Parçaları");
            } else if(Objects.equals(secilenValfTipi, "Kompanzasyon || İnişte Tek Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("3"), "Valf Blok Parçaları");
            }
        } else {
            //2 Grubu
            if(Objects.equals(secilenValfTipi, "İnişte Tek Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("4"), "Valf Blok Parçaları");
            } else if(Objects.equals(secilenValfTipi, "İnişte Çift Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("5"), "Valf Blok Parçaları");
            } else if(Objects.equals(secilenValfTipi, "Kilitli Blok")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("6"), "Valf Blok Parçaları");
            } else if(Objects.equals(secilenValfTipi, "Kompanzasyon || İnişte Tek Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.get("7"), "Valf Blok Parçaları");
            }
        }
    }

    private void loadBasincSalteriParca() {
        if(Objects.equals(basincSalteriDurumu, "Var")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaBasincSalteri.get("0"), "Basınç Şalteri Parçaları");
        }
    }

    private void loadElPompasiParca() {
        PartListTable separatorData = new PartListTable("----", "El Pompası Parçaları", "----");
        partListTable.getItems().add(separatorData);

        if(Objects.equals(elPompasiDurumu, "Var")) {
            String malzemeKodu = "150-51-10-086";
            String secilenMalzeme = "Oleocon Hidrolik El Pompası OHP Serisi 501-t";
            String adet = "1";

            PartListTable data = new PartListTable(malzemeKodu, secilenMalzeme, adet);
            partListTable.getItems().add(data);
        }
    }

    private void loadStandartParca() {
        if(Objects.equals(atananHT, "HT 40")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("0"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 70")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("1"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 100")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("2"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 125")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("3"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 160")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("4"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 200")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("5"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 250")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("6"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 300")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("7"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 350")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("8"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT 400")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("9"), "Standart Parçalar");
        } else if(Objects.equals(atananHT, "HT SOĞUTMA")) {
            generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaDefault.get("10"), "Standart Parçalar");
        }
    }

    private void loadSogutucuParca() {
        if(secilenHidrolikKilitDurumu.equals("Var")) {
            //Hidrolik Kilit Var
            if(secilenValfTipi.equals("İnişte Tek Hız") || secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaSogutma.get("2"), "Soğutucu Parçaları");
            } else if(secilenValfTipi.equals("İnişte Çift Hız") || secilenValfTipi.equals("Kilitli Blok")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaSogutma.get("3"), "Soğutucu Parçaları");
            }
        } else {
            //Hidrolik Kilit Yok
            if(secilenValfTipi.equals("İnişte Tek Hız") || secilenValfTipi.equals("Kompanzasyon || İnişte Tek Hız")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaSogutma.get("0"), "Soğutucu Parçaları");
            } else if(secilenValfTipi.equals("İnişte Çift Hız") || secilenValfTipi.equals("Kilitli Blok")) {
                generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaSogutma.get("1"), "Soğutucu Parçaları");
            }
        }
    }

    private void loadKilitMotorParca() {
        String kilitMotorVal = secilenKilitMotor;
        String kilitPompaVal = secilenKilitPompa;
        generalLoadFunc(SystemDefaults.getLocalHydraulicData().classicParcaKilitMotor.get("0"), "Kilit Motor Parçaları");

        if(kilitMotorVal.equals("1.5 kW")) {
            PartListTable data1 = new PartListTable("Veri Yok", kilitMotorVal + " Kilit Motor", "1");
            partListTable.getItems().add(data1);
        } else if(kilitMotorVal.equals("2.2 kW")) {
            PartListTable data1 = new PartListTable("Veri Yok", kilitMotorVal + " Kilit Motor", "1");
            partListTable.getItems().add(data1);
        }

        if(kilitPompaVal.equals("4.2 cc")) {
            PartListTable data1 = new PartListTable("Veri Yok", kilitPompaVal + " Pompa", "1");
            partListTable.getItems().add(data1);

            PartListTable data = new PartListTable("150-50-21-107", "CİVATA İMBUS M8 90 MM BEYAZ (DIN 912)", "2");
            partListTable.getItems().add(data);
        } else if(kilitPompaVal.equals("4.8 cc")) {
            PartListTable data1 = new PartListTable("Veri Yok", kilitPompaVal + " Pompa", "1");
            partListTable.getItems().add(data1);

            PartListTable data = new PartListTable("150-50-21-107", "CİVATA İMBUS M8 90 MM BEYAZ (DIN 912)", "2");
            partListTable.getItems().add(data);
        } else if(kilitPompaVal.equals("5.8 cc")) {
            PartListTable data1 = new PartListTable("Veri Yok", kilitPompaVal + " Pompa", "1");
            partListTable.getItems().add(data1);

            PartListTable data = new PartListTable("150-50-21-109", "CİVATA İMBUS M8 100 MM BEYAZ (DIN 912)", "2");
            partListTable.getItems().add(data);
        }
    }

    private void loadYagMiktari() {
        PartListTable separatorData = new PartListTable("----", "Hidrolik Yağ Parçaları", "----");
        partListTable.getItems().add(separatorData);

        String malzemeKodu = "150-53-04-002";
        String malzemeAdi = "HİDROLİK YAĞ SHELL TELLUS S2 M46";
        String adet = ClassicController.girilenTankKapasitesiMiktari + " Lt";

        PartListTable data = new PartListTable(malzemeKodu, malzemeAdi, adet);
        partListTable.getItems().add(data);
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
            "Ana sunucuya bağlanılamadı lütfen geliştirici ile iletişime geçin.", 
            "hidirektor@gmail.com");
    }
}
