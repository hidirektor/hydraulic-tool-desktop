package com.hidirektor.dashboard.controllers.pages.calculation;

import com.hidirektor.dashboard.Launcher;
import com.hidirektor.dashboard.controllers.notification.NotificationController;
import com.hidirektor.dashboard.utils.Model.Hydraulic.Kabin;
import com.hidirektor.dashboard.utils.Model.Table.DataControlTable;
import com.hidirektor.dashboard.utils.Notification.NotificationUtil;
import com.hidirektor.dashboard.utils.Process.UIProcess;
import com.hidirektor.dashboard.utils.System.SystemDefaults;
import com.hidirektor.dashboard.utils.Utils;
import com.hidirektor.dashboard.utils.Validation.ValidationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.ResourceBundle;

public class ClassicController implements Initializable  {

    @FXML
    public Label classicCaclulationTitle;

    @FXML
    public AnchorPane orderSection, unitInfoSection, calculationResultSection, calculationControlSection;

    @FXML
    public Button orderSectionButton, unitInfoSectionButton, calculationResultSectionButton, calculationControlSectionButton;

    @FXML
    public ImageView orderSectionButtonImage, unitInfoSectionButtonImage, calculationResultSectionButtonImage, calculationControlSectionButtonImage;

    //Hesaplama alanları:
    @FXML
    public TextField siparisNumarasiField, inviteUserTextField;

    @FXML
    public ComboBox<String> motorComboBox, sogutmaComboBox, hidrolikKilitComboBox, pompaComboBox, kompanzasyonComboBox, valfTipiComboBox, kilitMotorComboBox, kilitPompaComboBox;

    @FXML
    public TextField gerekenYagMiktariField;

    boolean isOrderSectionExpanded = false, isUnitInfoSectionExpanded = false, isCalculationResultSectionExpanded = false, isCalculationControlSectionExpanded = false;

    /*
    Sonuç için label ve imagelar
     */

    @FXML
    public Label tankTitle, tankOlculeriText, kabinTitle, disOlculerLabel, gecisOlculeriLabel, tankOlculeriLabel;

    @FXML
    public ImageView tankImage, schemeImage;

    @FXML
    public AnchorPane hydraulicUnitSchemePane;


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

            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, true);
        });
    }

    @FXML
    public void handleClick(ActionEvent actionEvent) {
        if(actionEvent.getSource().equals(orderSectionButton)) {
            collapseAndExpandSection(orderSection, isOrderSectionExpanded, orderSectionButtonImage, false);
            isOrderSectionExpanded = !isOrderSectionExpanded;
        } else if(actionEvent.getSource().equals(unitInfoSectionButton)) {
            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, false);
            isUnitInfoSectionExpanded = !isUnitInfoSectionExpanded;
        } else if(actionEvent.getSource().equals(calculationResultSectionButton)) {
            collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, false);
            isCalculationResultSectionExpanded = !isCalculationResultSectionExpanded;
        } else if(actionEvent.getSource().equals(calculationControlSectionButton)) {
            collapseAndExpandSection(calculationControlSection, isCalculationControlSectionExpanded, calculationControlSectionButtonImage, false);
            isCalculationControlSectionExpanded = !isCalculationControlSectionExpanded;
        } else {
            NotificationUtil.showNotification(orderSectionButton.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Buton Hatası", "Buton hatası meydana geldi. Lütfen yaptığınız işlemle birlikte hatayı bize bildirin.");
        }
    }

    private void comboBoxListener() {
        UIProcess.changeInputDataForTextField(siparisNumarasiField, newValue -> {
            girilenSiparisNumarasi = newValue;
            dataInit("motor", null);
            tabloGuncelle();

            collapseAndExpandSection(unitInfoSection, isUnitInfoSectionExpanded, unitInfoSectionButtonImage, true);
        });

        UIProcess.changeInputDataForComboBox(motorComboBox, newValue -> {
            secilenMotor = newValue;
            secilenKampana = Integer.parseInt(SystemDefaults.getLocalHydraulicData().motorKampanaMap.get(motorComboBox.getSelectionModel().getSelectedItem().toString()).replace(" mm", ""));

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

            if(secilenSogutmaDurumu.equals("Yok") && secilenHidrolikKilitDurumu.equals("Var") && kompanzasyonDurumu.equals("Var")) {
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
            } else {
                NotificationUtil.showNotification(classicCaclulationTitle.getScene().getWindow(), NotificationController.NotificationType.ALERT, "Hesaplama Hatası", "Hesaplama sonucu beklenmeyen bir hata oluştu.");
            }
        }
    }

    private void enableSonucSection() {
        collapseAndExpandSection(calculationResultSection, isCalculationResultSectionExpanded, calculationResultSectionButtonImage, true);
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
            addTextToList("X: " + x + " mm", 672, 445, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 475, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("0")), 560, 225, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("1")), 520, 290, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("2")), 510, 405, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("3")), 550, 420, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("4")), 575, 395, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("5")), 550, 370, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("6")), 650, 370, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("6")));
            addTextToList(secilenValfTipi, 570, 312, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 720, 300, 0, 12, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("7")), 775, 228, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("8")), 830, 290, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("9")), 810, 390, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("10")), 795, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("11")), 840, 383, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.get("11")));
        } else if (calculatedImage.equals("kilit_ayri_cift_hiz")) {
            addTextToList("X: " + x + " mm", 672, 448, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 475, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("0")), 565, 215, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("1")), 530, 255, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("2")), 595, 245, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("2")));
            addTextToList(getKampanaText(), 745, 260, 0, 10.5, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("3")), 785, 220, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("4")), 830, 263, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("5")), 595, 315, -90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("5")));
            addTextToList(secilenValfTipi, 570, 395, 0, 10, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("6")), 520, 415, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("7")), 640, 415, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("8")), 675, 385, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("9")), 740, 385, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("10")), 595, 427, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("11")), 675, 427, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("12")), 685, 408, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("13")), 795, 390, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("14")), 785, 425, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("15")), 835, 383, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.get("15")));
        } else if(calculatedImage.equals("kilit_ayri_tek_hiz")) {
            addTextToList("X: " + x + " mm", 672, 448, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 475, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("0")), 565, 215, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("1")), 530, 255, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("2")), 595, 245, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("2")));
            addTextToList(getKampanaText(), 745, 275, 0, 10.5, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("3")), 785, 220, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("4")), 830, 263, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("5")), 580, 315, -90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("6")), 525, 390, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("6")));
            addTextToList(secilenValfTipi, 615, 365, 0, 10, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("7")), 555, 427, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("8")), 675, 427, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("9")), 620, 410, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("10")), 690, 410, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("11")), 675, 385, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("12")), 740, 385, -30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("13")), 795, 390, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("14")), 785, 425, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("15")), 835, 383, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.get("15")));
        } else if(calculatedImage.equals("kilitli_blok")) {
            addTextToList("X: " + x + " mm", 672, 445, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 475, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("0")), 550, 225, 0, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("1")), 520, 285, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("2")), 560, 403, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("3")), 655, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("4")), 661, 385, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("4")));
            addTextToList(secilenValfTipi, 565, 305, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 725, 300, 0, 12, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("5")), 775, 228, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("6")), 830, 290, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("7")), 810, 390, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("8")), 795, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("9")), 840, 383, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.get("9")));
        } else if(calculatedImage.equals("tek_hiz_kompanzasyon_arti_tek_hiz")) {
            addTextToList("X: " + x + " mm", 672, 445, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 475, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("0")), 565, 225, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("1")), 520, 300, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("2")), 510, 405, 90, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("3")), 580, 395, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("4")), 545, 415, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("5")), 550, 370, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("6")), 660, 366, -45, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("6")));
            addTextToList(secilenValfTipi, 570, 340, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 720, 300, 0, 12, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("7")), 775, 228, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("8")), 830, 290, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("9")), 810, 390, 30, 10, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("10")), 795, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("11")), 840, 383, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.get("11")));
        } else if(calculatedImage.equals("sogutma_kilitsiz_cift_hiz")) {
            addTextToList("X: " + x + " mm", 672, 442, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 470, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("0")), 530, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("1")), 507, 372, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("2")), 565, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("3")), 590, 377, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("4")), 605, 323, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("5")), 560, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("6")), 612, 340, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("7")), 615, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("8")), 615, 255, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("9")), 665, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("10")), 730, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("11")), 810, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("12")), 843, 382, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("13")), 720, 365, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("14")), 736, 328, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("15")), 738, 400, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("16")), 780, 295, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("17")), 680, 250, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("18")), 710, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("19")), 810, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("20")), 785, 220, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("21")), 645, 226, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("22")), 815, 230, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("23")), 845, 240, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("24")), 815, 263, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("24")));
            addTextToList(secilenValfTipi, 535, 370, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 770, 355, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("25")), 630, 365, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.get("25")));
        } else if(calculatedImage.equals("sogutma_kilitli_cift_hiz")) {
            addTextToList("X: " + x + " mm", 672, 442, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 470, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("0")), 548, 230, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("1")), 507, 290, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("2")), 530, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("3")), 507, 372, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("4")), 565, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("5")), 590, 377, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("6")), 605, 323, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("7")), 560, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("8")), 612, 340, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("9")), 615, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("10")), 615, 255, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("11")), 665, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("12")), 730, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("13")), 810, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("14")), 843, 382, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("15")), 720, 365, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("16")), 736, 328, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("17")), 738, 400, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("18")), 780, 295, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("19")), 680, 250, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("20")), 710, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("21")), 810, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("22")), 785, 220, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("23")), 645, 226, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("24")), 815, 230, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("24")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("25")), 845, 240, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("25")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("26")), 815, 263, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("26")));
            addTextToList(secilenValfTipi, 535, 370, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 770, 355, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("27")), 630, 365, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.get("27")));
        } else if(calculatedImage.equals("sogutma_kilitsiz_tek_hiz")) {
            addTextToList("X: " + x + " mm", 672, 442, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 470, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("0")), 530, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("1")), 507, 372, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("2")), 565, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("3")), 590, 377, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("4")), 605, 323, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("5")), 560, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("6")), 612, 340, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("7")), 615, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("8")), 615, 255, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("9")), 665, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("10")), 730, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("11")), 810, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("12")), 843, 382, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("13")), 720, 365, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("14")), 736, 328, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("15")), 738, 400, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("16")), 780, 295, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("17")), 680, 250, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("18")), 710, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("19")), 810, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("20")), 785, 220, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("21")), 645, 226, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("22")), 815, 230, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("23")), 845, 240, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("24")), 815, 263, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("24")));
            addTextToList(secilenValfTipi, 535, 355, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 770, 355, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("25")), 630, 365, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.get("25")));
        } else if (calculatedImage.equals("sogutma_kilitli_tek_hiz")) {
            addTextToList("X: " + x + " mm", 672, 442, 0, 14, Color.BLACK, null);
            addTextToList("Y: " + y + " mm", 470, 318, 90, 14, Color.BLACK, null);

            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("0")), 548, 230, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("0")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("1")), 507, 290, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("1")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("2")), 530, 330, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("2")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("3")), 507, 372, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("3")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("4")), 565, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("4")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("5")), 590, 377, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("5")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("6")), 605, 323, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("6")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("7")), 560, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("7")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("8")), 612, 340, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("8")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("9")), 615, 305, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("9")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("10")), 615, 255, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("10")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("11")), 665, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("11")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("12")), 730, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("12")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("13")), 810, 420, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("13")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("14")), 843, 382, 90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("14")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("15")), 720, 365, -90, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("15")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("16")), 736, 328, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("16")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("17")), 738, 400, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("17")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("18")), 780, 295, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("18")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("19")), 680, 250, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("19")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("20")), 710, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("20")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("21")), 810, 280, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("21")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("22")), 785, 220, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("22")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("23")), 645, 226, 0, 11, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("23")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("24")), 815, 230, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("24")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("25")), 845, 240, 90, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("25")));
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("26")), 815, 263, 0, 9, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("26")));
            addTextToList(secilenValfTipi, 535, 355, 0, 10, Color.BLACK, null);
            addTextToList(getKampanaText(), 770, 355, 0, 9, Color.BLACK, null);
            addTextToList(SystemDefaults.getLocalHydraulicData().getValue(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("27")), 630, 365, 0, 8, Color.BLACK, SystemDefaults.getLocalHydraulicData().getKey(SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.get("27")));
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

    private void collapseAndExpandSection(AnchorPane targetPane, boolean isExpanded, ImageView targetImageView, boolean forceToOpen) {
        Image arrowDown = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_arrow_down.png")));
        Image arrowUp = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/icons/icon_arrow_up.png")));

        if(forceToOpen) {
            isExpanded = true;
            targetPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            targetPane.setVisible(true);
            targetImageView.setImage(arrowUp);
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
