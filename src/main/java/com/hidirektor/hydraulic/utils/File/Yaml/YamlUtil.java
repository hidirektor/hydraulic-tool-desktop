package com.hidirektor.hydraulic.utils.File.Yaml;

import com.hidirektor.hydraulic.utils.Model.Hydraulic.Motor;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class YamlUtil {

    public YamlUtil(String classicComboPath, String powerPackComboPath, String classicPartPath, String powerpackPartHidrosPath, String powerpackPartIthalPath, String schematicTextsPath) {
        loadClassicCombo(classicComboPath);
        loadPowerPackCombo(powerPackComboPath);
        loadClassicPartList(classicPartPath);
        loadPowerPackHidrosPartList(powerpackPartHidrosPath);
        loadPowerPackIthalPartList(powerpackPartIthalPath);
        loadSchematicTexts(schematicTextsPath);
    }

    public void loadClassicCombo(String filePath) {
        try {
            loadMotor(filePath);
            loadSogutma(filePath);
            loadHydraulicLock(filePath);
            loadPompa(filePath);
            loadKompanzasyon(filePath);
            loadValfTipi(filePath);
            loadKilitMotor(filePath);
            loadKilitPompa(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPowerPackCombo(String filePath) {
        try {
            loadMotorVoltaj(filePath);
            loadUniteTipi(filePath);
            loadMotorGucu(filePath);
            loadPowerPackPompa(filePath);
            loadTankTipi(filePath);
            loadTankKapasitesi(filePath);
            loadPlatformTipi(filePath);
            loadPowerPackValfTipi(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadClassicPartList(String filePath) {
        try {
            loadClassicParcaMotor(filePath);
            loadClassicParcaKampana(filePath);
            loadClassicParcaPompa(filePath);
            loadClassicParcaKaplin(filePath);
            loadClassicParcaValfBloklari(filePath);
            loadClassicParcaSogutma(filePath);
            loadClassicParcaBasincSalteri(filePath);
            loadClassicParcaDefault(filePath);
            loadClassicParcaKilitMotor(filePath);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPowerPackHidrosPartList(String filePath) {
        try {
            loadPowerPackParcaMotor380(filePath, "hidros");
            loadPowerPackParcaMotor220(filePath, "hidros");
            loadPowerPackParcaPompa(filePath, "hidros");
            loadPowerPackParcaTankDikey(filePath, "hidros");
            loadPowerPackParcaTankYatay(filePath, "hidros");
            loadPowerPackParcaESPGenel(filePath, "hidros");
            loadPowerPackParcaESPDikeyCift(filePath, "hidros");
            loadPowerPackParcaDevirmeli(filePath, "hidros");
            loadPowerPackParcaStandart(filePath, "hidros");
            loadPowerPackParcaOzelYatay(filePath, "hidros");
            loadPowerPackParcaValfTipleri(filePath, "hidros");
            loadPowerPackParcaOzelCiftValf(filePath, "hidros");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPowerPackIthalPartList(String filePath) {
        try {
            loadPowerPackParcaMotor380(filePath, "ithal");
            loadPowerPackParcaMotor220(filePath, "ithal");
            loadPowerPackParcaPompa(filePath, "ithal");
            loadPowerPackParcaTankDikey(filePath, "ithal");
            loadPowerPackParcaTankYatay(filePath, "ithal");
            loadPowerPackParcaESPGenel(filePath, "ithal");
            loadPowerPackParcaESPDikeyCift(filePath, "ithal");
            loadPowerPackParcaDevirmeli(filePath, "ithal");
            loadPowerPackParcaStandart(filePath, "ithal");
            loadPowerPackParcaOzelYatay(filePath, "ithal");
            loadPowerPackParcaValfTipleri(filePath, "ithal");
            loadPowerPackParcaOzelCiftValf(filePath, "ithal");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSchematicTexts(String filePath) {
        try {
            loadCiftHizTexts(filePath);
            loadKilitAyriCiftHizTexts(filePath);
            loadKilitAyriTekHizTexts(filePath);
            loadKilitliBlokTexts(filePath);
            loadTekHizKompanzasyonArtiTekHizTexts(filePath);
            loadSogutmaKilitsizCiftHizTexts(filePath);
            loadSogutmaKilitliCiftHizTexts(filePath);
            loadSogutmaKilitsizTekHizTexts(filePath);
            loadSogutmaKilitliTekHizTexts(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMotor(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);
        Map<String, Map<String, Map<String, String>>> motorData = (Map<String, Map<String, Map<String, String>>>) yamlData.get("motor");

        motorData.forEach((key, value) -> {
            LinkedList<String> motorList = new LinkedList<>();
            value.forEach((innerKey, motorDetails) -> {
                String motorName = motorDetails.get("name");
                motorList.add(motorName);

                // Kampana ve yükseklik map'lerine atama
                SystemDefaults.getLocalHydraulicData().motorKampanaMap.put(motorName, motorDetails.get("kampana"));
                SystemDefaults.getLocalHydraulicData().motorYukseklikMap.put(motorName, motorDetails.get("yukseklik"));
            });
            SystemDefaults.getLocalHydraulicData().motorMap.put(key, motorList);
        });
        input.close();
    }

    public void loadSogutma(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> sogutma = (Map<String, Object>) yamlData.get("sogutma");
        if (sogutma != null) {
            for (String key : sogutma.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) sogutma.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().coolingMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadHydraulicLock(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> hydraulicLock = (Map<String, Object>) yamlData.get("hidrolik_kilit");
        if (hydraulicLock != null) {
            for (String key : hydraulicLock.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) hydraulicLock.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().hydraulicLockMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadPompa(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Map<String, Object>> pompaData = (Map<String, Map<String, Object>>) yamlData.get("pompa");

        pompaData.forEach((key, value) -> {
            LinkedList<String> pompaList = new LinkedList<>();

            Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) value.get("options");

            options.forEach((innerKey, pompaDetails) -> {
                String pompaName = pompaDetails.get("name");
                pompaList.add(pompaName);
            });

            SystemDefaults.getLocalHydraulicData().pumpMap.put(key, pompaList);
        });
        input.close();
    }

    public void loadKompanzasyon(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> kompanzasyon = (Map<String, Object>) yamlData.get("kompanzasyon");
        if (kompanzasyon != null) {
            for (String key : kompanzasyon.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) kompanzasyon.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().compensationMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadValfTipi(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Map<String, Object>> valfTipiData = (Map<String, Map<String, Object>>) yamlData.get("valf_tipi");

        valfTipiData.forEach((key, value) -> {
            LinkedList<String> valfTipiList = new LinkedList<>();

            Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) value.get("options");

            options.forEach((innerKey, valfTipiDetails) -> {
                String valfTipiName = valfTipiDetails.get("value");
                valfTipiList.add(valfTipiName);
            });

            SystemDefaults.getLocalHydraulicData().valveTypeMap.put(key, valfTipiList);
        });
        input.close();
    }

    public void loadKilitMotor(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> kilitMotor = (Map<String, Object>) yamlData.get("kilit_motor");
        if (kilitMotor != null) {
            for (String key : kilitMotor.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) kilitMotor.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().lockMotorMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadKilitPompa(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> kilitPompa = (Map<String, Object>) yamlData.get("kilit_pompa");
        if (kilitPompa != null) {
            for (String key : kilitPompa.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) kilitPompa.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().lockPumpMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadMotorVoltaj(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> motorVoltaj = (Map<String, Object>) yamlData.get("motor");
        if (motorVoltaj != null) {
            for (String key : motorVoltaj.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) motorVoltaj.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().motorVoltajMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadUniteTipi(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> uniteTipi = (Map<String, Object>) yamlData.get("unite_tipi");
        if (uniteTipi != null) {
            for (String key : uniteTipi.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) uniteTipi.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().uniteTipiMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadMotorGucu(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Map<String, Object>> motorGucuData = (Map<String, Map<String, Object>>) yamlData.get("motor_gucu");

        motorGucuData.forEach((key, value) -> {
            LinkedList<Motor> motorList = new LinkedList<>();

            Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) value.get("options");

            options.forEach((innerKey, motorDetails) -> {
                String motorName = motorDetails.get("name");
                String motorYukseklik = motorDetails.get("motorYukseklik");
                motorList.add(new Motor(motorName, motorYukseklik));
            });

            SystemDefaults.getLocalHydraulicData().motorGucuMap.put(key, motorList);
        });
        input.close();
    }

    public void loadPowerPackPompa(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Map<String, Object>> pompaData = (Map<String, Map<String, Object>>) yamlData.get("pompa");

        pompaData.forEach((key, value) -> {
            LinkedList<String> pompaList = new LinkedList<>();

            Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) value.get("options");

            options.forEach((innerKey, pompaDetails) -> {
                String pompaName = pompaDetails.get("value");
                pompaList.add(pompaName);
            });

            SystemDefaults.getLocalHydraulicData().pompaPowerPackMap.put(key, pompaList);
        });
        input.close();
    }

    public void loadTankTipi(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> tankTipi = (Map<String, Object>) yamlData.get("tank_tipi");
        if (tankTipi != null) {
            for (String key : tankTipi.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) tankTipi.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().tankTipiMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadTankKapasitesi(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Map<String, Object>> tankKapasitesiData = (Map<String, Map<String, Object>>) yamlData.get("tank_kapasitesi");

        tankKapasitesiData.forEach((key, value) -> {
            LinkedList<String> tankKapasitesiList = new LinkedList<>();

            Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) value.get("options");

            options.forEach((innerKey, tankKapasitesiDetails) -> {
                String tankKapasitesiName = tankKapasitesiDetails.get("value");
                tankKapasitesiList.add(tankKapasitesiName);
            });

            SystemDefaults.getLocalHydraulicData().tankKapasitesiMap.put(key, tankKapasitesiList);
        });
        input.close();
    }

    public void loadPlatformTipi(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> platformTipi = (Map<String, Object>) yamlData.get("platform_tipi");
        if (platformTipi != null) {
            for (String key : platformTipi.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) platformTipi.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().platformTipiMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadPowerPackValfTipi(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        Map<String, Object> valfTipi = (Map<String, Object>) yamlData.get("valf_tipleri");
        if (valfTipi != null) {
            for (String key : valfTipi.keySet()) {
                Map<String, Object> options = (Map<String, Object>) ((Map<String, Object>) valfTipi.get(key)).get("options");
                LinkedList<String> valuesList = new LinkedList<>();
                for (String optionKey : options.keySet()) {
                    Object valueMap = options.get(optionKey);
                    if (valueMap instanceof Map) {
                        valuesList.add(((Map<String, String>) valueMap).get("value"));
                    } else if (valueMap instanceof String) {
                        valuesList.add((String) valueMap);
                    }
                }
                SystemDefaults.getLocalHydraulicData().valfTipiMap.put(key, valuesList);
            }
        }
        input.close();
    }

    public void loadClassicParcaMotor(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("motor");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaMotor.put(kaplinKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaKampana(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> kampanaData = (Map<String, Object>) yamlData.get("kampana");

            for (Map.Entry<String, Object> kampanaEntry : kampanaData.entrySet()) {
                String kampanaKey = kampanaEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kampanaEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaKampana.put(kampanaKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaPompa(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> pompaData = (Map<String, Object>) yamlData.get("pompa");

            for (Map.Entry<String, Object> pompaEntry : pompaData.entrySet()) {
                String pompaKey = pompaEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) pompaEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaPompa.put(pompaKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaKaplin(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> kaplinData = (Map<String, Object>) yamlData.get("kaplin");

            for (Map.Entry<String, Object> kaplinEntry : kaplinData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaKaplin.put(kaplinKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaValfBloklari(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("valf_bloklari");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaValfBloklari.put(kaplinKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaSogutma(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("sogutma");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaSogutma.put(kaplinKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaBasincSalteri(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("basinc_salteri");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaBasincSalteri.put(kaplinKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaDefault(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("standart_malzemeler");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaDefault.put(kaplinKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadClassicParcaKilitMotor(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("kilit_motor");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                SystemDefaults.getLocalHydraulicData().classicParcaKilitMotor.put(kaplinKey, partDetailsList);
            }
        }
        input.close();
    }

    public void loadPowerPackParcaMotor380(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("motor_380");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor380.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor380.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaMotor220(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("motor_220");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaMotor220.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaMotor220.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaPompa(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("pompa");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaPompa.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaPompa.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaTankDikey(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("tank_dikey");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankDikey.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankDikey.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaTankYatay(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("tank_yatay");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaTankYatay.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaTankYatay.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaESPGenel(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("esp_default");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPGenel.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPGenel.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaESPDikeyCift(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("esp_custom");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaESPCiftHiz.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaESPCiftHiz.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaDevirmeli(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("devirmeli_yuruyus");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaDevirmeli.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaDevirmeli.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaStandart(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("standart_malzemeler");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaDefault.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaDefault.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaOzelYatay(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("ozel_yatay");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaOzelYatayGenel.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaOzelYatayGenel.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaValfTipleri(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("valf_types");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaValf.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaValf.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadPowerPackParcaOzelCiftValf(String filePath, String unitType) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> valfData = (Map<String, Object>) yamlData.get("ozel_cift_valf");

            for (Map.Entry<String, Object> kaplinEntry : valfData.entrySet()) {
                String kaplinKey = kaplinEntry.getKey();
                Map<String, Object> partsData = (Map<String, Object>) kaplinEntry.getValue();
                Map<String, Object> parts = (Map<String, Object>) partsData.get("parts");

                LinkedList<String> partDetailsList = new LinkedList<>();

                for (Map.Entry<String, Object> partEntry : parts.entrySet()) {
                    Map<String, String> partDetails = (Map<String, String>) partEntry.getValue();

                    String malzemeKodu = partDetails.get("malzemeKodu");
                    String malzemeAdi = partDetails.get("malzemeAdi");
                    String malzemeAdet = partDetails.get("malzemeAdet");

                    String combinedDetails = malzemeKodu + ";" + malzemeAdi + ";" + malzemeAdet;

                    partDetailsList.add(combinedDetails);
                }

                if(Objects.equals(unitType, "hidros")) {
                    SystemDefaults.getLocalHydraulicData().powerPackHidrosParcaOzelCiftValf.put(kaplinKey, partDetailsList);
                } else {
                    SystemDefaults.getLocalHydraulicData().powerPackIthalParcaOzelCiftValf.put(kaplinKey, partDetailsList);
                }
            }
        }
        input.close();
    }

    public void loadCiftHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("cift_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicCiftHizTexts.put(key, combinedValue);
            }
        }
        input.close();
    }

    public void loadKilitAyriCiftHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("kilit_ayri_cift_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicKilitAyriCiftHizTexts.put(key, combinedValue);
            }
        }
        input.close();
    }

    public void loadKilitAyriTekHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("kilit_ayri_tek_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicKilitAyriTekHizTexts.put(key, combinedValue);
            }
        }
        input.close();
    }

    public void loadKilitliBlokTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("kilitli_blok");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicKilitliBlokTexts.put(key, combinedValue);
            }
        }
        input.close();
    }

    public void loadTekHizKompanzasyonArtiTekHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("tek_hiz_kompanzasyon_arti_tek_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicTekHizKompanzasyonArtiTekHizTexts.put(key, combinedValue);
            }
        }

        input.close();
    }

    public void loadSogutmaKilitsizCiftHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("sogutma_kilitsiz_cift_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizCiftHizTexts.put(key, combinedValue);
            }
        }

        input.close();
    }

    public void loadSogutmaKilitliCiftHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("sogutma_kilitli_cift_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliCiftHizTexts.put(key, combinedValue);
            }
        }

        input.close();
    }

    public void loadSogutmaKilitsizTekHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("sogutma_kilitsiz_tek_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitsizTekHizTexts.put(key, combinedValue);
            }
        }
        input.close();
    }

    public void loadSogutmaKilitliTekHizTexts(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(input);

        if (yamlData != null) {
            Map<String, Object> ciftHizData = (Map<String, Object>) yamlData.get("sogutma_kilitli_tek_hiz");

            for (Map.Entry<String, Object> entry : ciftHizData.entrySet()) {
                String key = entry.getKey();
                Map<String, String> details = (Map<String, String>) entry.getValue();

                String ciftHizKey = details.get("key");
                String ciftHizValue = details.get("value");

                String combinedValue = ciftHizKey + ";" + ciftHizValue;
                SystemDefaults.getLocalHydraulicData().schematicSogutmaKilitliTekHizTexts.put(key, combinedValue);
            }
        }
        input.close();
    }
}