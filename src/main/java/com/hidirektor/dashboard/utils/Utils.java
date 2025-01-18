package com.hidirektor.dashboard.utils;

import com.hidirektor.dashboard.utils.Model.Hydraulic.Kabin;
import com.hidirektor.dashboard.utils.System.SystemDefaults;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class Utils {

    public static void clickButton(Button actionButton, int clickCount) {
        MouseEvent mousePressedEvent = new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, MouseButton.PRIMARY, clickCount, false, false, false, false, false, false, false, false, false, false, null);
        actionButton.fireEvent(mousePressedEvent);
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
