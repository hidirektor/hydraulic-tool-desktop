package com.hidirektor.hydraulic.utils;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.utils.Model.Hydraulic.Kabin;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

public class Utils {

    public static void clickButton(Button actionButton, int clickCount) {
        MouseEvent mousePressedEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, MouseButton.PRIMARY, clickCount, false, false, false, false, false, false, false, false, false, false, null);
        actionButton.fireEvent(mousePressedEvent);
    }

    public static void showPopup(Screen currentScreen, String fxmlPath, String title, Modality popupModality, StageStyle popupStyle) {
        Image icon = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/logos/onderlift-hydraulic-logo.png")));

        AtomicReference<Double> screenX = new AtomicReference<>((double) 0);
        AtomicReference<Double> screenY = new AtomicReference<>((double) 0);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource(fxmlPath));
            VBox root = fxmlLoader.load();

            Stage popupStage = new Stage();
            Rectangle2D bounds = currentScreen.getVisualBounds();
            popupStage.setOnShown(event -> {
                double stageWidth = popupStage.getWidth();
                double stageHeight = popupStage.getHeight();

                double centerX = bounds.getMinX() + (bounds.getWidth() - stageWidth) / 2;
                double centerY = bounds.getMinY() + (bounds.getHeight() - stageHeight) / 2;

                popupStage.setX(centerX);
                popupStage.setY(centerY);
            });

            popupStage.initModality(popupModality);
            if(popupStyle != null) {
                popupStage.initStyle(popupStyle);
            }
            popupStage.setTitle(title);
            popupStage.setScene(new Scene(root));
            popupStage.getIcons().add(icon);

            root.setOnMousePressed(event -> {
                screenX.set(event.getSceneX());
                screenY.set(event.getSceneY());
            });
            root.setOnMouseDragged(event -> {

                popupStage.setX(event.getScreenX() - screenX.get());
                popupStage.setY(event.getScreenY() - screenY.get());

            });
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentUnixTime() {
        long unixTime = Instant.now().getEpochSecond();
        return String.valueOf(unixTime);
    }

    public static double stringToDouble(String inputVal) {
        String[] secPmp = inputVal.split(" cc");

        return Double.parseDouble(secPmp[0]);
    }

    public static Kabin findClassicTankByKabinName(String kabinName) {
        for (Kabin tank : SystemDefaults.getLocalHydraulicData().classicCabins) {
            if (tank.getKabinName().equals(kabinName)) {
                return tank;
            }
        }
        return null;
    }

    public static Kabin findPowerPackTankByKabinName(String kabinName) {
        for (Kabin tank : SystemDefaults.getLocalHydraulicData().powerPackCabins) {
            if (tank.getKabinName().equals(kabinName)) {
                return tank;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void createLocalUnitData(String yamlFilePath, String orderNumber, String createdDate, String unitType,
                                           String pdfPath, String excelPath, String isOffline, String createdBy, JSONObject unitParameters) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        File yamlFile = new File(yamlFilePath);
        if (!yamlFile.exists()) {
            data = new HashMap<>();
            data.put("local_units", new HashMap<>());
        } else {
            try (FileReader reader = new FileReader(yamlFile)) {
                data = yaml.load(reader);
                if (data == null) {
                    data = new HashMap<>();
                    data.put("local_units", new HashMap<>());
                }
            } catch (IOException e) {
                throw new RuntimeException("YAML dosyası okunurken bir hata oluştu", e);
            }
        }

        Map<String, Map<String, Object>> localUnits = (Map<String, Map<String, Object>>) data.get("local_units");

        boolean found = false;
        for (Map.Entry<String, Map<String, Object>> entry : localUnits.entrySet()) {
            if (entry.getValue().get("order_number").equals(orderNumber)) {
                if (pdfPath != null) entry.getValue().put("pdf_path", pdfPath);
                if (excelPath != null) entry.getValue().put("excel_path", excelPath);
                found = true;
                break;
            }
        }

        if (!found) {
            int nextIndex = localUnits.size();
            Map<String, Object> newEntry = new HashMap<>();
            newEntry.put("order_number", orderNumber);
            newEntry.put("created_date", createdDate);
            newEntry.put("unit_type", unitType);
            newEntry.put("pdf_path", pdfPath == null ? "" : pdfPath);
            newEntry.put("excel_path", excelPath == null ? "" : excelPath);

            if (unitParameters != null) {
                newEntry.put("unit_parameters", compress(unitParameters.toString()));
            }

            Map<String, String> creationData = new HashMap<>();
            creationData.put("isOffline", isOffline);
            creationData.put("created_by", createdBy);

            newEntry.put("creation_data", creationData);

            localUnits.put(String.valueOf(nextIndex), newEntry);
        }

        try (FileWriter writer = new FileWriter(yamlFile)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            yaml = new Yaml(options);
            yaml.dump(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("YAML dosyasına yazılırken bir hata oluştu", e);
        }
    }

    // JSON sıkıştırma
    public static String compress(String jsonString) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(jsonString.getBytes());
            gzipOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("JSON sıkıştırma sırasında bir hata oluştu", e);
        }
    }

    // JSON açma
    public static String decompress(String compressedString) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] compressedBytes = Base64.getDecoder().decode(compressedString);
            try (java.util.zip.GZIPInputStream gzipInputStream = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(compressedBytes))) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                return byteArrayOutputStream.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("JSON açma sırasında bir hata oluştu", e);
        }
    }
}
