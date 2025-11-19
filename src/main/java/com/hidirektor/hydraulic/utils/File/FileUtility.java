package com.hidirektor.hydraulic.utils.File;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.utils.File.JSON.JSONUtil;
import com.hidirektor.hydraulic.utils.File.Yaml.YamlUtil;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import javafx.application.Platform;
import me.t3sl4.util.file.DirectoryUtil;
import me.t3sl4.util.file.FileUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileUtility {
    public static void criticalFileSystem() {
        // İşletim sistemine göre dosya yollarını ayarla
        String userHome = System.getProperty("user.name");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            SystemDefaults.basePath = "C:/Users/" + userHome + "/";
        } else {
            SystemDefaults.basePath = "/Users/" + userHome + "/";
        }



        // Dosya yollarını belirle
        SystemDefaults.baseFolderPath = SystemDefaults.basePath + "OnderGrup/";
        SystemDefaults.userDataFolderPath = SystemDefaults.baseFolderPath + "userData/";
        SystemDefaults.hydraulicFileDataFolderPath = SystemDefaults.userDataFolderPath + "HydraulicUnits/";
        SystemDefaults.programDataPath = SystemDefaults.baseFolderPath + "data/";

        SystemDefaults.userDataPDFFolderPath = SystemDefaults.hydraulicFileDataFolderPath + "schematicFiles/";
        SystemDefaults.userDataExcelFolderPath = SystemDefaults.hydraulicFileDataFolderPath + "excelFiles/";

        SystemDefaults.accountDataFilePath = SystemDefaults.userDataFolderPath + "auth.txt";
        SystemDefaults.accountLicenseFilePath = SystemDefaults.userDataFolderPath + "license.txt";
        SystemDefaults.userLocalUnitDataFilePath = SystemDefaults.hydraulicFileDataFolderPath + "local_units.yml";

        SystemDefaults.generalDBPath = SystemDefaults.programDataPath + "general.json";
        SystemDefaults.cabinsDBPath = SystemDefaults.programDataPath + "cabins.json";
        SystemDefaults.classicComboDBPath = SystemDefaults.programDataPath + "classic_combo.yml";
        SystemDefaults.powerPackComboDBPath = SystemDefaults.programDataPath + "powerpack_combo.yml";
        SystemDefaults.blainComboDBPath = SystemDefaults.programDataPath + "blain_combo.yml";
        SystemDefaults.classicPartsDBPath = SystemDefaults.programDataPath + "classic_parts.yml";
        SystemDefaults.powerPackPartsHidrosDBPath = SystemDefaults.programDataPath + "powerpack_parts_hidros.yml";
        SystemDefaults.powerPackPartsIthalDBPath = SystemDefaults.programDataPath + "powerpack_parts_ithal.yml";
        SystemDefaults.schematicTextsDBPath = SystemDefaults.programDataPath + "schematic_texts.yml";
        SystemDefaults.partOriginsClassicDBPath = SystemDefaults.programDataPath + "part_origins_classic.yml";
        SystemDefaults.partOriginsPowerPackDBPath = SystemDefaults.programDataPath + "part_origins_powerpack.yml";

        try {
            DirectoryUtil.createDirectory(SystemDefaults.baseFolderPath);
            DirectoryUtil.createDirectory(SystemDefaults.userDataFolderPath);
            DirectoryUtil.createDirectory(SystemDefaults.hydraulicFileDataFolderPath);
            DirectoryUtil.createDirectory(SystemDefaults.programDataPath);

            DirectoryUtil.createDirectory(SystemDefaults.userDataPDFFolderPath);
            DirectoryUtil.createDirectory(SystemDefaults.userDataExcelFolderPath);

            FileUtil.createFile(SystemDefaults.accountDataFilePath);
            FileUtil.createFile(SystemDefaults.accountLicenseFilePath);
            FileUtil.createFile(SystemDefaults.userLocalUnitDataFilePath);

            copyResourceFile("/assets/data/programDatabase/general.json", SystemDefaults.generalDBPath, false);
            copyResourceFile("/assets/data/programDatabase/cabins.json", SystemDefaults.cabinsDBPath, false);
            copyResourceFile("/assets/data/programDatabase/classic_combo.yml", SystemDefaults.classicComboDBPath, false);
            copyResourceFile("/assets/data/programDatabase/powerpack_combo.yml", SystemDefaults.powerPackComboDBPath, false);
            copyResourceFile("/assets/data/programDatabase/blain_combo.yml", SystemDefaults.blainComboDBPath, false);
            copyResourceFile("/assets/data/programDatabase/classic_parts.yml", SystemDefaults.classicPartsDBPath, false);
            copyResourceFile("/assets/data/programDatabase/powerpack_parts_hidros.yml", SystemDefaults.powerPackPartsHidrosDBPath, false);
            copyResourceFile("/assets/data/programDatabase/powerpack_parts_ithal.yml", SystemDefaults.powerPackPartsIthalDBPath, false);
            copyResourceFile("/assets/data/programDatabase/schematic_texts.yml", SystemDefaults.schematicTextsDBPath, false);
            copyResourceFile("/assets/data/programDatabase/part_origins_classic.yml", SystemDefaults.partOriginsClassicDBPath, false);
            copyResourceFile("/assets/data/programDatabase/part_origins_powerpack.yml", SystemDefaults.partOriginsPowerPackDBPath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setupLocalData() {
        JSONUtil.loadJSONData();
        new YamlUtil(SystemDefaults.classicComboDBPath, SystemDefaults.powerPackComboDBPath, SystemDefaults.blainComboDBPath, SystemDefaults.classicPartsDBPath, SystemDefaults.powerPackPartsHidrosDBPath, SystemDefaults.powerPackPartsIthalDBPath, SystemDefaults.schematicTextsDBPath);
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> FileUtility.partRenameAutomatically("klasik"));
            Platform.runLater(() -> FileUtility.partRenameAutomatically("powerpack"));
        }).start();
    }

    public static void partRenameAutomatically(String partType) {
        Set<String> modifiedFiles = new HashSet<>();

        try {
            Map<String, Object> partOrigins;

            String[] TARGET_FILES;

            if(partType.equals("klasik")) {
                partOrigins = loadYamlFile(SystemDefaults.partOriginsClassicDBPath);

                TARGET_FILES = new String[]{
                        SystemDefaults.classicPartsDBPath
                };
            } else {
                partOrigins = loadYamlFile(SystemDefaults.partOriginsPowerPackDBPath);

                TARGET_FILES = new String[]{
                        SystemDefaults.powerPackPartsHidrosDBPath,
                        SystemDefaults.powerPackPartsIthalDBPath
                };
            }

            for (String targetFilePath : TARGET_FILES) {
                Map<String, Object> targetData = loadYamlFile(targetFilePath);

                boolean isModifiedByName = updateParts(targetData, partOrigins, true);

                boolean isModifiedByCode = updateParts(targetData, partOrigins, false);

                if (isModifiedByName || isModifiedByCode) {
                    saveYamlFile(targetFilePath, targetData);
                    modifiedFiles.add(targetFilePath);
                }
            }

            //System.out.println("Güncelleme tamamlandı.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Dosya işlemleri sırasında bir hata oluştu.");
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean updateParts(Map<String, Object> targetData, Map<String, Object> partOrigins, boolean matchByName) {
        boolean isModified = false;

        // part_origins verisini al
        Map<String, Object> originsParts = (Map<String, Object>) partOrigins.get("part_origins");

        if (originsParts != null) {
            for (Object originPartKey : originsParts.keySet()) {
                Map<String, Object> originPart = (Map<String, Object>) originsParts.get(originPartKey);

                if (originPart != null) {
                    // parts içindeki malzemeKodu ve malzemeAdi'ni almak için iç içe yapıyı kontrol et
                    Map<String, Object> parts = (Map<String, Object>) originPart.get("parts");
                    if (parts != null && parts.containsKey("0")) {
                        Map<String, Object> part = (Map<String, Object>) parts.get("0");
                        String originCode = (String) part.get("malzemeKodu");
                        String originName = (String) part.get("malzemeAdi");

                        if (originName == null || originCode == null) {
                            System.err.println("Hata: part_origins içerisinde eksik veri var. originName veya originCode null.");
                            continue; // Eksik veri varsa bu kaydı atla
                        }

                        // TARGET dosyasındaki motor veya ozel_cift_valf yapılarını kontrol et
                        for (Object targetKey : targetData.keySet()) {
                            Map<String, Object> targetPart = (Map<String, Object>) targetData.get(targetKey);

                            if (targetPart != null) {
                                // motor formatındaki dosyalar için
                                //System.out.println(targetPart);
                                for (Object outerKey : targetPart.keySet()) {
                                    Map<String, Object> outerPart = (Map<String, Object>) targetPart.get(outerKey);
                                    //System.out.println("Dış Anahtar: " + outerKey + ", Değer: " + outerPart);

                                    if (outerPart != null && outerPart.containsKey("parts")) {
                                        Map<String, Object> targetParts = (Map<String, Object>) outerPart.get("parts");

                                        // "parts" içindeki her bir sayı key'ini kontrol et
                                        for (Object innerKey : targetParts.keySet()) {
                                            Map<String, Object> currentPart = (Map<String, Object>) targetParts.get(innerKey);
                                            //System.out.println("parts içindeki Anahtar: " + innerKey + ", Değer: " + currentPart);

                                            if (currentPart != null && currentPart.containsKey("malzemeKodu") && currentPart.containsKey("malzemeAdi")) {
                                                String targetName = (String) currentPart.get("malzemeAdi");
                                                String targetCode = (String) currentPart.get("malzemeKodu");

                                                //System.out.println("Hedef İsim: " + targetName);
                                                //System.out.println("Hedef Kod: " + targetCode);

                                                if (targetName == null || targetCode == null) {
                                                    System.err.println("Hata: Hedef dosyada eksik veri var. targetName veya targetCode null.");
                                                    continue; // Eksik veri varsa bu kaydı atla
                                                }

                                                // Güncelleme mantığı
                                                if (matchByName && originName.equals(targetName) && !originCode.equals(targetCode)) {
                                                    currentPart.put("malzemeKodu", originCode); // malzemeKodu'nu güncelle
                                                    isModified = true;
                                                } else if (!matchByName && originCode.equals(targetCode) && !originName.equals(targetName)) {
                                                    currentPart.put("malzemeAdi", originName); // malzemeAdi'ni güncelle
                                                    isModified = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return isModified;
    }

    private static Map<String, Object> loadYamlFile(String filePath) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            Map<String, Object> yamlData = yaml.load(inputStream);
            //System.out.println("YAML Dosyası İçeriği:");
            //System.out.println(yamlData);  // Bu şekilde dosya içeriğini yazdırabilirsiniz
            return yamlData;
        }
    }

    private static void saveYamlFile(String filePath, Map<String, Object> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(filePath)) {
            yaml.dump(data, writer);
        }
    }

    public static boolean copyResourceFile(String resourcePath, String destPath, boolean isOverwrite) throws IOException {
        // Kaynak dosyayı stream olarak al
        InputStream inputStream = Launcher.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }

        Path destination = Paths.get(destPath);
        if (!isOverwrite && Files.exists(destination)) {
            return false;
        }

        // Hedef dosyaya yaz
        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
        return true;
    }
}
