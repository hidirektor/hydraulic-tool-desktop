package com.hidirektor.hydraulic.utils.Process;

import com.hidirektor.hydraulic.controllers.pages.calculation.ClassicController;
import com.hidirektor.hydraulic.controllers.pages.calculation.PowerPackController;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public class UIProcess {

    public static void changeInputDataForTextField(TextField targetField, Consumer<String> successConsumer) {
        targetField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.equals(oldValue)) {
                    successConsumer.accept(newValue);
                }
            }
        });
    }

    public static void changeInputDataForComboBox(ComboBox targetCombo, Consumer<String> successConsumer, Runnable isDataExistRunnable) {
        targetCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if(!targetCombo.getItems().isEmpty()) {
                if(newValue != null) {
                    if(oldValue != newValue) {
                        if(oldValue == null) {
                            if(isDataExistRunnable != null) {
                                isDataExistRunnable.run();
                            }
                        }

                        successConsumer.accept(newValue.toString());
                    }
                }
            }
        });
    }

    public static void clearOldCalculationData() {
        /*
        Ekran değişimi sonrası Klasik temizliği
         */
        ClassicController.girilenSiparisNumarasi = null;
        ClassicController.secilenMotor = null;
        ClassicController.kompanzasyonDurumu = null;
        ClassicController.secilenKampana = 0;
        ClassicController.secilenPompa = null;
        ClassicController.girilenTankKapasitesiMiktari = 0;
        ClassicController.secilenHidrolikKilitDurumu = null;
        ClassicController.secilenValfTipi = null;
        ClassicController.secilenKilitMotor = null;
        ClassicController.secilenKilitPompa = null;
        ClassicController.secilenSogutmaDurumu = null;

        /*
        Ekran değişimi sonrası PowerPack temizliği
         */
        PowerPackController.secilenMotorTipi = null;
        PowerPackController.secilenMotorGucu = null;
        PowerPackController.secilenPompa = null;
        PowerPackController.uniteTipiDurumu = null;
        PowerPackController.secilenTankTipi = null;
        PowerPackController.secilenTankKapasitesi = null;
        PowerPackController.secilenOzelTankGenislik = null;
        PowerPackController.secilenOzelTankYukseklik = null;
        PowerPackController.secilenOzelTankDerinlik = null;
        PowerPackController.secilenBirinciValf = null;
        PowerPackController.secilenInisTipi = null;
        PowerPackController.secilenPlatformTipi = null;
        PowerPackController.secilenIkinciValf = null;
        PowerPackController.kabinKodu = null;
    }
}
