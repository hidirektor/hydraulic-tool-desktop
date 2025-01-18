package com.hidirektor.hydraulic.utils.Model.Hydraulic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class HydraulicData {
    public int kampanaTankArasiBoslukX;
    public int kampanaTankArasiBoslukY;
    public int kampanaBoslukYOn;
    public int tekHizTankArasiBoslukX;
    public int tekHizTankArasiBoslukY;
    public int tekHizAraBoslukX;
    public int tekHizYOn;
    public int tekHizBlokX;
    public int tekHizBlokY;
    public int ciftHizTankArasiBoslukX;
    public int ciftHizTankArasiBoslukY;
    public int ciftHizAraBoslukX;
    public int ciftHizYOn;
    public int ciftHizBlokX;
    public int ciftHizBlokY;
    public int kilitliBlokTankArasiBoslukX;
    public int kilitliBlokTankArasiBoslukY;
    public int kilitliBlokAraBoslukX;
    public int kilitliBlokYOn;
    public int kilitliBlokX;
    public int kilitliBlokY;
    public int kilitMotorTankArasiBoslukX;
    public int kilitMotorTankArasiBoslukY;
    public int kilitMotorAraBoslukX;
    public int kilitMotorYOn;
    public int tekHizKilitAyriY;
    public int tekHizKilitAyriYOn;
    public int ciftHizKilitAyriY;
    public int ciftHizKilitAyriYOn;
    public int kilitMotorX;
    public int kilitMotorY;
    public int kayipLitre;
    public int defaultHeight;

    //TODO
    //Yeni sistem değişkenleri

    public ArrayList<String> uniteTipiDegerleri = new ArrayList<>(); //Jsondan gelen değerler
    public LinkedList<Kabin> classicCabins = new LinkedList<>(); //Jsondan gelen tank verileri
    public LinkedList<Kabin> powerPackCabins = new LinkedList<>(); //Jsondan gelen tank verileri

    /*
    Classic Side
     */
    public Map<String, LinkedList<String>> motorMap = new HashMap<>();
    public Map<String, String> motorKampanaMap = new HashMap<>();
    public Map<String, String> motorYukseklikMap = new HashMap<>();

    public Map<String, LinkedList<String>> coolingMap = new HashMap<>();
    public Map<String, LinkedList<String>> hydraulicLockMap = new HashMap<>();
    public Map<String, LinkedList<String>> pumpMap = new HashMap<>();
    public Map<String, LinkedList<String>> compensationMap = new HashMap<>();
    public Map<String, LinkedList<String>> valveTypeMap = new HashMap<>();
    public Map<String, LinkedList<String>> lockMotorMap = new HashMap<>();
    public Map<String, LinkedList<String>> lockPumpMap = new HashMap<>();

    /*
    Power Pack Side
     */
    public Map<String, LinkedList<String>> motorVoltajMap = new HashMap<>();
    public Map<String, LinkedList<String>> uniteTipiMap = new HashMap<>();
    public Map<String, LinkedList<Motor>> motorGucuMap = new HashMap<>();
    public Map<String, LinkedList<String>> pompaPowerPackMap = new HashMap<>();
    public Map<String, LinkedList<String>> tankTipiMap = new HashMap<>();
    public Map<String, LinkedList<String>> tankKapasitesiMap = new HashMap<>();
    public Map<String, LinkedList<String>> platformTipiMap = new HashMap<>();
    public Map<String, LinkedList<String>> valfTipiMap = new HashMap<>();

    /*
    Parça Listesi
     */

    /*
    Classic Parça Listesi
     */
    public Map<String, LinkedList<String>> classicParcaMotor = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaKampana = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaPompa = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaKaplin = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaValfBloklari = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaSogutma = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaBasincSalteri = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaDefault = new HashMap<>();
    public Map<String, LinkedList<String>> classicParcaKilitMotor = new HashMap<>();

    /*
    PowerPack Hidros Parça Listesi
     */
    public Map<String, LinkedList<String>> powerPackHidrosParcaMotor380 = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaMotor220 = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaPompa = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaTankDikey = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaTankYatay = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaESPGenel = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaESPCiftHiz = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaDevirmeli = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaDefault = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaOzelYatayGenel = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaOzelCiftValf = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackHidrosParcaValf = new HashMap<>();

    /*
    PowerPack İthal Parça Listesi
     */
    public Map<String, LinkedList<String>> powerPackIthalParcaMotor380 = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaMotor220 = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaPompa = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaTankDikey = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaTankYatay = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaESPGenel = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaESPCiftHiz = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaDevirmeli = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaDefault = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaOzelYatayGenel = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaOzelCiftValf = new HashMap<>();
    public Map<String, LinkedList<String>> powerPackIthalParcaValf = new HashMap<>();

    //2D Texts
    public HashMap<String, String> schematicCiftHizTexts = new HashMap<>();
    public HashMap<String, String> schematicKilitAyriCiftHizTexts = new HashMap<>();
    public HashMap<String, String> schematicKilitAyriTekHizTexts = new HashMap<>();
    public HashMap<String, String> schematicKilitliBlokTexts = new HashMap<>();
    public HashMap<String, String> schematicTekHizKompanzasyonArtiTekHizTexts = new HashMap<>();
    public HashMap<String, String> schematicSogutmaKilitsizCiftHizTexts = new HashMap<>();
    public HashMap<String, String> schematicSogutmaKilitliCiftHizTexts = new HashMap<>();
    public HashMap<String, String> schematicSogutmaKilitsizTekHizTexts = new HashMap<>();
    public HashMap<String, String> schematicSogutmaKilitliTekHizTexts = new HashMap<>();

    public HydraulicData() {
        //TODO
        //data update
    }

    public String getKey(String combinedValue) {
        if (combinedValue != null && combinedValue.contains(";")) {
            return combinedValue.split(";")[0]; // ";" ile ayırıp ilk kısmı döndür
        }
        return null;
    }

    public String getValue(String combinedValue) {
        if (combinedValue != null && combinedValue.contains(";")) {
            return combinedValue.split(";")[1]; // ";" ile ayırıp ikinci kısmı döndür
        }
        return null;
    }
}